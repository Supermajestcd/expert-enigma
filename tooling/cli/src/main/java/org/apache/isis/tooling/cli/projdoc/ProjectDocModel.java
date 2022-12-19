/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.tooling.cli.projdoc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.structurizr.model.Container;

import org.apache.commons.lang3.builder.EqualsExclude;
import org.asciidoctor.ast.Document;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Files;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.graph._Graph;
import org.apache.isis.tooling.c4.C4;
import org.apache.isis.tooling.cli.CliConfig.ProjectDoc;
import org.apache.isis.tooling.javamodel.AnalyzerConfigFactory;
import org.apache.isis.tooling.model4adoc.AsciiDocFactory;
import org.apache.isis.tooling.model4adoc.AsciiDocWriter;
import org.apache.isis.tooling.projectmodel.ArtifactCoordinates;
import org.apache.isis.tooling.projectmodel.Dependency;
import org.apache.isis.tooling.projectmodel.ProjectNode;

import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.block;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.cell;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.doc;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.headRow;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.row;
import static org.apache.isis.tooling.model4adoc.AsciiDocFactory.table;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import guru.nidi.codeassert.config.Language;
import guru.nidi.codeassert.model.CodeClass;
import guru.nidi.codeassert.model.Model;

/**
 * Acts both as a model and a writer (adoc).
 * @since Sep 22, 2020
 *
 */
public class ProjectDocModel {

    private final ProjectNode projTree;
    private SortedSet<ProjectNode> modules;

    public ProjectDocModel(ProjectNode projTree) {
        this.projTree = projTree;
    }

    public String toAsciiDoc(ProjectDoc projectDocConfig) {

        modules = new TreeSet<ProjectNode>();
        projTree.depthFirst(modules::add);

        val doc = doc();
        doc.setTitle("System Overview");

        _Strings.nonEmpty(projectDocConfig.getLicenseHeader())
        .ifPresent(block(doc)::setSource);
        
        _Strings.nonEmpty(projectDocConfig.getDescription())
        .ifPresent(block(doc)::setSource);

        projectDocConfig.getArtifactGroups().forEach((section, groupId)->{
            createSection(doc, section, groupId);
        });

        if(!modules.isEmpty()) {
            createSection(doc, "Other", null);
        }

        try {
            return AsciiDocWriter.toString(doc);
        } catch (IOException e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        } 

    }

    // -- HELPER

    @RequiredArgsConstructor(staticName = "of")
    @EqualsAndHashCode
    private static class ProjectAndContainerTuple {
        final ProjectNode projectNode;
        @EqualsExclude final Container container;
    }
    
    private static class GroupDiagram {
        
        private final C4 c4;
        private final List<ProjectNode> projectNodes = new ArrayList<>(); 

        public GroupDiagram(C4 c4) {
            this.c4 = c4;
        }

        public void collect(ProjectNode module) {
            projectNodes.add(module);
        }

        //XXX lombok issues, not using val here
        public String toPlantUml() {
            val key = c4.getWorkspaceName();
            val softwareSystem = c4.softwareSystem("package-ecosystem", null);

            final Can<ProjectAndContainerTuple> tuples = Can.<ProjectNode>ofCollection(projectNodes)
            .map(projectNode->{
                val name = projectNode.getName();
                val description = ""; //projectNode.getDescription() XXX needs sanitizing, potentially breaks plantuml/asciidoc syntax
                val technology = String.format("packaging: %s", projectNode.getArtifactCoordinates().getPackaging());
                val container = softwareSystem.addContainer(name, description, technology);
                return ProjectAndContainerTuple.of(projectNode, container);
            });

            
            final _Graph<ProjectAndContainerTuple> adjMatrix = 
                    _Graph.of(tuples, (a, b)->a.projectNode.getChildren().contains(b.projectNode));

            tuples.forEach(tuple->{
                adjMatrix.streamNeighbors(tuple)
                .forEach(dependentTuple->{
                    tuple.container.uses(dependentTuple.container, "");
                });
            });

            val containerView = c4.getViewSet().createContainerView(softwareSystem, key, "Artifact Dependency Diagram (Maven)");
            containerView.addAllContainers();

            val plantUmlSource = c4.toPlantUML(containerView);
            return plantUmlSource;
        }

        public String toAsciiDoc() {
            val key = c4.getWorkspaceName();

            return AsciiDocFactory.SourceFactory.plantuml(toPlantUml(), key, null);
        }

    }

    private void createSection(
            final @NonNull Document doc, 
            final @NonNull String sectionName, 
            final @Nullable String groupIdPattern) {

        val titleBlock = block(doc);

        titleBlock.setSource(String.format("== %s", sectionName));

        val descriptionBlock = block(doc);
        val groupDiagram = new GroupDiagram(C4.of(sectionName, null));

        val table = table(doc);
        table.setTitle(String.format("Projects/Modules (%s)", sectionName));
        table.setAttribute("cols", "2m,2m,1m,1m,2,5a", true);
        table.setAttribute("header-option", "", true);

        val headRow = headRow(table);

        cell(table, headRow, "Group");
        cell(table, headRow, "Artifact");
        cell(table, headRow, "Type");
        cell(table, headRow, "Folder");
        cell(table, headRow, "Name");
        cell(table, headRow, "Description");

        val projRoot = _Files.canonicalPath(projTree.getProjectDirectory()).get();

        Set<ProjectNode> modulesWritten = new HashSet<>();

        modules.stream()
        .filter(module->matchesGroupId(module, groupIdPattern))
        .forEach(module->{

            val projPath = _Files.canonicalPath(module.getProjectDirectory()).get();
            val projRelativePath = _Files.toRelativePath(projRoot, projPath);

            modulesWritten.add(module);
            groupDiagram.collect(module);

            val row = row(table);
            cell(table, row, module.getArtifactCoordinates().getGroupId());
            cell(table, row, module.getArtifactCoordinates().getArtifactId());
            cell(table, row, module.getArtifactCoordinates().getPackaging());
            cell(table, row, projRelativePath);
            cell(table, row, module.getName());
            cell(table, row, details(module));
        });

        descriptionBlock.setSource(groupDiagram.toAsciiDoc());

        modules.removeAll(modulesWritten);

    }

    private boolean matchesGroupId(ProjectNode module, String groupIdPattern) {
        if(_Strings.isNullOrEmpty(module.getArtifactCoordinates().getGroupId())) {
            return false; // never match on missing data
        }
        if(_Strings.isNullOrEmpty(groupIdPattern)) {
            return true; // no groupIdPattern, always matches
        }
        if(groupIdPattern.equals(module.getArtifactCoordinates().getGroupId())) {
            return true; // exact match
        }
        if(groupIdPattern.endsWith(".*")) {
            val groupIdPrefix = groupIdPattern.substring(0, groupIdPattern.length()-2);
            if(groupIdPrefix.equals(module.getArtifactCoordinates().getGroupId())) {
                return true; // exact match
            }
            if(groupIdPrefix.equals(module.getArtifactCoordinates().getGroupId())) {
                return true; // exact prefix match
            }
            if(module.getArtifactCoordinates().getGroupId().startsWith(groupIdPrefix+".")) {
                return true; // prefix match
            }
        }
        return false;
    }

    private String details(ProjectNode module) {
        val description = module.getDescription().trim();
        val dependencyList = module.getDependencies()
                .stream()
                .map(Dependency::getArtifactCoordinates)
                .map(ArtifactCoordinates::toString)
                .map(ProjectDocModel::toAdocListItem)
                .collect(Collectors.joining())
                .trim();
        val componentList = gatherSpringComponents(module.getProjectDirectory())
                .stream()
                .map(ProjectDocModel::toAdocListItem)
                .collect(Collectors.joining())
                .trim();

        val sb = new StringBuilder();

        if(!description.isEmpty()) {
            sb.append(description).append("\n\n");
        }

        if(!componentList.isEmpty()) {
            sb.append(toAdocSection("Components", componentList));
        }

        if(!dependencyList.isEmpty()) {
            sb.append(toAdocSection("Dependencies", dependencyList));
        }

        return sb.toString();
    }

    private static String toAdocSection(String title, String content) {
        return String.format("_%s_\n\n%s\n\n", title, content);
    }

    private static String toAdocListItem(String element) {
        return String.format("* %s\n", element);
    }

    private SortedSet<String> gatherSpringComponents(File projDir) {

        val analyzerConfig = AnalyzerConfigFactory.maven(projDir, Language.JAVA).main();

        val model = Model.from(analyzerConfig.getClasses()).read();

        SortedSet<String> components = model.getClasses()
                .stream()
                .filter(codeClass->codeClass
                        .getAnnotations()
                        .stream()
                        .map(CodeClass::getName)
                        .anyMatch(name->name.startsWith("org.springframework.stereotype.")))
                .map(CodeClass::getName)
                //.peek(System.out::println) //debug
                .collect(Collectors.toCollection(TreeSet::new));

        return components;
    }

}

