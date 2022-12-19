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
package org.apache.isis.config;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.ioc.spring._Spring;
import org.apache.isis.metamodel.facets.actions.action.command.CommandActionsConfiguration;
import org.apache.isis.metamodel.facets.actions.action.publishing.PublishActionsConfiguration;
import org.apache.isis.metamodel.facets.object.domainobject.auditing.AuditObjectsConfiguration;
import org.apache.isis.metamodel.facets.object.domainobject.auditing.DefaultViewConfiguration;
import org.apache.isis.metamodel.facets.object.domainobject.editing.EditingObjectsConfiguration;
import org.apache.isis.metamodel.facets.object.domainobject.publishing.PublishObjectsConfiguration;
import org.apache.isis.metamodel.facets.properties.property.command.CommandPropertiesConfiguration;
import org.apache.isis.metamodel.facets.properties.property.publishing.PublishPropertiesConfiguration;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeaturesInitConfiguration;
import org.apache.isis.metamodel.specloader.IntrospectionMode;
import org.apache.isis.viewer.wicket.ui.DialogMode;

import lombok.Data;
import lombok.Getter;


/**
 * 
 * Configuration 'beans' with meta-data (IDE-support).
 * 
 * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/configuration-metadata.html">spring.io</a>
 * @apiNote should ultimately replace {@link IsisConfigurationLegacy}
 * 
 * @since 2.0
 *
 */
@ConfigurationProperties(ConfigurationConstants.ROOT_PREFIX)
@Data
public class IsisConfiguration {

    @Autowired
    private ConfigurableEnvironment environment;
    
    /**
     * Not populated by Spring!
     * @deprecated maybe using {@link #environment} is the better choice!?
     */
    @Getter(lazy = true) @Deprecated
    private final Map<String, String> rawKeyValueMap = _Spring.copySpringEnvironmentToMap(environment);
    
    private final Authentication authentication = new Authentication();
    @Data
    public static class Authentication {
        private final Shiro shiro = new Shiro();
        @Data
        public static class Shiro {
            private boolean autoLogoutIfAlreadyAuthenticated = false;
        }
    }

    private final Objects objects = new Objects();
    @Data
    public static class Objects {
        private EditingObjectsConfiguration editing = EditingObjectsConfiguration.TRUE;
    }

    private final Persistor persistor = new Persistor();
    @Data
    public static class Persistor {
        private final Datanucleus datanucleus = new Datanucleus();
        @Data
        public static class Datanucleus {
            private String classMetadataLoadedListener = "org.apache.isis.jdo.datanucleus.CreateSchemaObjectFromClassMetadata";

            private boolean installFixtures = false;

            private final Impl impl = new Impl();
            @Data
            public static class Impl {
                private final Javax javax = new Javax();
                @Data
                public static class Javax {
                    private final Jdo jdo = new Jdo();
                    @Data
                    public static class Jdo {
                        private final Option option = new Option();
                        @Data
                        public static class Option {
                            // this field also appears in additional-spring-configuration-metadata.json, to fix the casing as 'ConnectionDriverName'
                            private String connectionDriverName;
                            // this field also appears in additional-spring-configuration-metadata.json, to fix the casing as 'ConnectionURL'
                            private String connectionUrl;
                            // this field also appears in additional-spring-configuration-metadata.json, to fix the casing as 'ConnectionUserName'
                            private String connectionUserName;
                            // this field also appears in additional-spring-configuration-metadata.json, to fix the casing as 'ConnectionPassword'
                            private String connectionPassword;
                        }
                    }
                }
            }

            private final StandaloneCollection standaloneCollection = new StandaloneCollection();
            @Data
            public static class StandaloneCollection {
                private boolean bulkLoad = false;
            }
        }
        /**
         * Default is <code>false</code> only for backward compatibility (to avoid lots of breakages in existing code);
         * in future might change to <code>true</code>.
         *
         * <p>
         *     currently disabled (in ISIS-921); to reinstate in ISIS-922? else delete.
         * </p>
         *
         * @deprecated
         */
        @Deprecated
        private boolean enforceSafeSemantics = false;
    }

    private final Reflector reflector = new Reflector();
    @Data
    public static class Reflector {

        private final ExplicitAnnotations explicitAnnotations = new ExplicitAnnotations();
        @Data
        public static class ExplicitAnnotations {

            /**
             * Whether or not a public method needs to be annotated with
             * @{@link org.apache.isis.applib.annotation.Action} in order to be picked up as an action in the metamodel.
             */
            private boolean action = false;
        }

        private final Facet facet = new Facet();
        @Data
        public static class Facet {

            private boolean filterVisibility = true;

            private final ActionAnnotation actionAnnotation = new ActionAnnotation();
            @Data
            public static class ActionAnnotation {
                private final DomainEvent domainEvent = new DomainEvent();
                @Data
                public static class DomainEvent {
                    private boolean postForDefault = true;
                }
            }

            private final CollectionAnnotation collectionAnnotation = new CollectionAnnotation();
            @Data
            public static class CollectionAnnotation {
                private final DomainEvent domainEvent = new DomainEvent();
                @Data
                public static class DomainEvent {
                    private boolean postForDefault = true;
                }
            }

            private final CssClass cssClass = new CssClass();
            @Data
            public static class CssClass {
                private Map<Pattern, String> patterns = new HashMap<>();
            }
            private final CssClassFa cssClassFa = new CssClassFa();
            @Data
            public static class CssClassFa {
                private Map<Pattern, String> patterns = new HashMap<>();
            }


            private final DomainObjectAnnotation domainObjectAnnotation = new DomainObjectAnnotation();
            @Data
            public static class DomainObjectAnnotation {
                private final CreatedLifecycleEvent createdLifecycleEvent = new CreatedLifecycleEvent();
                @Data
                public static class CreatedLifecycleEvent {
                    private boolean postForDefault = true;
                }
                private final LoadedLifecycleEvent loadedLifecycleEvent = new LoadedLifecycleEvent();
                @Data
                public static class LoadedLifecycleEvent {
                    private boolean postForDefault = true;
                }
                private final PersistingLifecycleEvent persistingLifecycleEvent = new PersistingLifecycleEvent();
                @Data
                public static class PersistingLifecycleEvent {
                    private boolean postForDefault = true;
                }
                private final PersistedLifecycleEvent persistedLifecycleEvent = new PersistedLifecycleEvent();
                @Data
                public static class PersistedLifecycleEvent {
                    private boolean postForDefault = true;
                }
                private final RemovingLifecycleEvent removingLifecycleEvent = new RemovingLifecycleEvent();
                @Data
                public static class RemovingLifecycleEvent {
                    private boolean postForDefault = true;
                }
                private final UpdatedLifecycleEvent updatedLifecycleEvent = new UpdatedLifecycleEvent();
                @Data
                public static class UpdatedLifecycleEvent {
                    private boolean postForDefault = true;
                }
                private final UpdatingLifecycleEvent updatingLifecycleEvent = new UpdatingLifecycleEvent();
                @Data
                public static class UpdatingLifecycleEvent {
                    private boolean postForDefault = true;
                }
            }

            private final DomainObjectLayoutAnnotation domainObjectLayoutAnnotation = new DomainObjectLayoutAnnotation();
            @Data
            public static class DomainObjectLayoutAnnotation {
                private final CssClassUiEvent cssClassUiEvent = new CssClassUiEvent();
                @Data
                public static class CssClassUiEvent {
                    private boolean postForDefault = true;
                }
                private final IconUiEvent iconUiEvent = new IconUiEvent();
                @Data
                public static class IconUiEvent {
                    private boolean postForDefault = true;
                }
                private final LayoutUiEvent layoutUiEvent = new LayoutUiEvent();
                @Data
                public static class LayoutUiEvent {
                    private boolean postForDefault = true;
                }
                private final TitleUiEvent titleUiEvent = new TitleUiEvent();
                @Data
                public static class TitleUiEvent {
                    private boolean postForDefault = true;
                }
            }

            private final PropertyAnnotation propertyAnnotation = new PropertyAnnotation();
            @Data
            public static class PropertyAnnotation {
                private final DomainEvent domainEvent = new DomainEvent();
                @Data
                public static class DomainEvent {
                    private boolean postForDefault = true;
                }
            }
            private final ViewModelLayoutAnnotation viewModelLayoutAnnotation = new ViewModelLayoutAnnotation();
            @Data
            public static class ViewModelLayoutAnnotation {
                private final CssClassUiEvent cssClassUiEvent = new CssClassUiEvent();
                @Data
                public static class CssClassUiEvent {
                    private boolean postForDefault =true;
                }
                private final IconUiEvent iconUiEvent = new IconUiEvent();
                @Data
                public static class IconUiEvent {
                    private boolean postForDefault =true;
                }
                private final LayoutUiEvent layoutUiEvent = new LayoutUiEvent();
                @Data
                public static class LayoutUiEvent {
                    private boolean postForDefault =true;
                }
                private final TitleUiEvent titleUiEvent = new TitleUiEvent();
                @Data
                public static class TitleUiEvent {
                    private boolean postForDefault =true;
                }
            }
        }

        private final Facets facets = new Facets();
        @Data
        public static class Facets {
            private final ViewModelSemanticCheckingFacetFactory viewModelSemanticCheckingFacetFactory = new ViewModelSemanticCheckingFacetFactory();
            @Data
            public static class ViewModelSemanticCheckingFacetFactory {
                private boolean enable = false;
            }
            private boolean ignoreDeprecated = false;
        }

        private final Introspector introspector = new Introspector();
        @Data
        public static class Introspector {
            private boolean parallelize = true;
            private IntrospectionMode mode = IntrospectionMode.LAZY_UNLESS_PRODUCTION;
        }

        private final Validator validator = new Validator();
        @Data
        public static class Validator {

            private boolean allowDeprecated = true;
            private boolean ensureUniqueObjectTypes = true;
            private boolean checkModuleExtent = true;
            private boolean noParamsOnly = false;
            private boolean actionCollectionParameterChoices = true;
            @Deprecated
            private boolean serviceActionsOnly = true;
            @Deprecated
            private boolean mixinsOnly = true;
            private boolean explicitObjectType = false;

            private boolean jaxbViewModelNotAbstract = true;
            private boolean jaxbViewModelNotInnerClass = true;
            private boolean jaxbViewModelNoArgConstructor = false;
            private boolean jaxbViewModelReferenceTypeAdapter = true;
            private boolean jaxbViewModelDateTimeTypeAdapter = true;

            private boolean jdoqlFromClause = true;
            private boolean jdoqlVariablesClause = true;

        }
    }

    private final Service service = new Service();
    @Data
    public static class Service {
        private final Email email = new Email();
        @Data
        public static class Email {
            private int port = 587;
            private int socketConnectionTimeout = 2000;
            private int socketTimeout = 2000;
            private boolean throwExceptionOnFail = true;
            private final Override override = new Override();
            @Data
            public static class Override {
                private String to;
                private String cc;
                private String bcc;
            }
            private final Sender sender = new Sender();
            @Data
            public static class Sender {
                private String username;
                private String address;
                private String password;
                private String hostname;
            }
            private final Tls tls = new Tls();
            @Data
            public static class Tls {
                private boolean enabled = true;
            }
        }
    }

    private final Services services = new Services();
    @Data
    public static class Services {

        private final ApplicationFeatures applicationFeatures = new ApplicationFeatures();
        @Data
        public static class ApplicationFeatures {
            ApplicationFeaturesInitConfiguration init = ApplicationFeaturesInitConfiguration.NOT_SPECIFIED;
        }

        private final Audit audit = new Audit();
        @Data
        public static class Audit {
            private AuditObjectsConfiguration objects = AuditObjectsConfiguration.NONE;
        }
        
        private final Command command = new Command();
        @Data
        public static class Command {
            private CommandActionsConfiguration actions = CommandActionsConfiguration.NONE;
            private CommandPropertiesConfiguration properties = CommandPropertiesConfiguration.NONE;
        }

        private final Container container = new Container();
        @Data
        public static class Container {

            /**
             * Normally any queries are automatically preceded by flushing pending executions.
             *
             * <p>
             * This key allows this behaviour to be disabled.
             *
             * <p>
             *     Originally introduced as part of ISIS-1134 (fixing memory leaks in the objectstore)
             *     where it was found that the autoflush behaviour was causing a (now unrepeatable)
             *     data integrity error (see <a href="https://issues.apache.org/jira/browse/ISIS-1134?focusedCommentId=14500638&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-14500638">ISIS-1134 comment</a>, in the isis-module-security.
             *     However, that this could be circumvented by removing the call to flush().
             *     We don't want to break existing apps that might rely on this behaviour, on the
             *     other hand we want to fix the memory leak.  Adding this configuration property
             *     seems the most prudent way forward.
             * </p>
             */
            private boolean disableAutoFlush = false;

        }

        private final ExceptionRecognizerCompositeForJdoObjectStore exceptionRecognizerCompositeForJdoObjectStore = new ExceptionRecognizerCompositeForJdoObjectStore();
        @Data
        public static class ExceptionRecognizerCompositeForJdoObjectStore {
            private boolean disable = false;
        }

        private final Injector injector = new Injector();
        @Data
        public static class Injector {
            private boolean setPrefix = false;
            private boolean injectPrefix = true;
        }

        private final Publish publish = new Publish();
        @Data
        public static class Publish {
            private PublishActionsConfiguration actions = PublishActionsConfiguration.NONE;
            private PublishObjectsConfiguration objects = PublishObjectsConfiguration.NONE;
            private PublishPropertiesConfiguration properties = PublishPropertiesConfiguration.NONE;
        }

        private final Translation translation = new Translation();
        @Data
        public static class Translation {

            private final Po po = new Po();

            @Data
            public static class Po {

                TranslationService.Mode mode = TranslationService.Mode.WRITE;
            }

        }
    }

    private final Viewer viewer = new Viewer();
    @Data
    public static class Viewer {
        private final Restfulobjects restfulobjects = new Restfulobjects();
        @Data
        public static class Restfulobjects {
            private String basePath = "/restful";
            private boolean honorUiHints = false;
            private boolean objectPropertyValuesOnly = false;
            private boolean strictAcceptChecking = false;
            private boolean suppressDescribedByLinks = false;
            private boolean suppressMemberDisabledReason = false;
            private boolean suppressMemberExtensions = false;
            private boolean suppressMemberId = false;
            private boolean suppressMemberLinks = false;
            private boolean suppressUpdateLink = false;
            private final Gsoc2013 gsoc2013 = new Gsoc2013();
            @Data
            public static class Gsoc2013 {
                private boolean legacyParamDetails = false;
            }
        }

        private final Wicket wicket = new Wicket();
        @Data
        public static class Wicket {

            private String app = "org.apache.isis.viewer.wicket.viewer.IsisWicketApplication";

            /**
             * Whether the Ajax debug should be shown.
             */
            private boolean ajaxDebugMode = false;

            private String basePath = "/wicket";

            private boolean clearOriginalDestination = false;

            /**
             * The pattern used for rendering and parsing dates.
             *
             * <p>
             * Each Date scalar panel will use {@ #getDatePattern()} or {@linkplain #getDateTimePattern()} depending on its
             * date type.  In the case of panels with a date picker, the pattern will be dynamically adjusted so that it can be
             * used by the <a href="https://github.com/Eonasdan/bootstrap-datetimepicker">Bootstrap Datetime Picker</a>
             * component (which uses <a href="http://momentjs.com/docs/#/parsing/string-format/">Moment.js formats</a>, rather
             * than those of regular Java code).
             */
            private String datePattern = "dd-MM-yyyy";

            /**
             * The pattern used for rendering and parsing date/times.
             *
             * <p>
             * Each Date scalar panel will use {@link Wicket#getDatePattern()} or {@link Wicket#getDateTimePattern()} depending on its
             * date type.  In the case of panels with a date time picker, the pattern will be dynamically adjusted so that it can be
             * used by the <a href="https://github.com/Eonasdan/bootstrap-datetimepicker">Bootstrap Datetime Picker</a>
             * component (which uses <a href="http://momentjs.com/docs/#/parsing/string-format/">Moment.js formats</a>, rather
             * than those of regular Java code).
             */
            private String dateTimePattern = "dd-MM-yyyy HH:mm";

            private DialogMode dialogMode = DialogMode.SIDEBAR;

            private DialogMode dialogModeForMenu = DialogMode.MODAL;

            private String liveReloadUrl;

            private int maxTitleLengthInTables = 12;

            private Integer maxTitleLengthInParentedTables;
            public int getMaxTitleLengthInParentedTables() {
                return maxTitleLengthInParentedTables != null ? maxTitleLengthInParentedTables : getMaxTitleLengthInTables();
            }
            /**
             * The maximum length that a title of an object will be shown when rendered in a parented table;
             * will be truncated beyond this (with ellipses to indicate the truncation).
             */
            public void setMaxTitleLengthInParentedTables(final int val) {
                maxTitleLengthInParentedTables = val;
            }

            private Integer maxTitleLengthInStandaloneTables;
            public int getMaxTitleLengthInStandaloneTables() {
                return maxTitleLengthInStandaloneTables != null ? maxTitleLengthInStandaloneTables : getMaxTitleLengthInTables();
            }
            /**
             * The maximum length that a title of an object will be shown when rendered in a standalone table;
             * will be truncated beyond this (with ellipses to indicate the truncation).
             */
            public void setMaxTitleLengthInStandaloneTables(final int val) {
                maxTitleLengthInStandaloneTables = val;
            }

            /**
             * Whether to use a modal dialog for property edits and for actions associated with properties.
             * This can be overridden on a case-by-case basis using <code>@PropertyLayout#promptStyle</code> and
             * <code>@ActionLayout#promptStyle</code>.
             *
             * This behaviour is disabled by default; the viewer will use an inline prompt in these cases, making for a smoother
             * user experience. If enabled then this reinstates the pre-1.15.0 behaviour of using a dialog prompt in all cases.
             */
            private PromptStyle promptStyle = PromptStyle.INLINE;

            /**
             * Whether to redirect to a new page, even if the object being shown (after an action invocation or a property edit)
             * is the same as the previous page.
             *
             * This behaviour is disabled by default; the viewer will update the existing page if it can, making for a
             * smoother user experience. If enabled then this reinstates the pre-1.15.0 behaviour of redirecting in all cases.
             */
            private boolean redirectEvenIfSameObject = false;

            /**
             * in Firefox and more recent versions of Chrome 54+, cannot copy out of disabled fields; instead we use the
             * readonly attribute (https://www.w3.org/TR/2014/REC-html5-20141028/forms.html#the-readonly-attribute)
             * This behaviour is enabled by default but can be disabled using this flag
             */
            private boolean replaceDisabledTagWithReadonlyTag = true;

            /**
             * Whether to disable a form submit button after it has been clicked, to prevent users causing an error if they
             * do a double click.
             *
             * This behaviour is enabled by default, but can be disabled using this flag.
             */
            private boolean preventDoubleClickForFormSubmit = true;

            /**
             * Whether to disable a no-arg action button after it has been clicked, to prevent users causing an error if they
             * do a double click.
             *
             * This behaviour is enabled by default, but can be disabled using this flag.
             */
            private boolean preventDoubleClickForNoArgAction = true;

            private boolean showFooter = true;

            /**
             * Whether Wicket tags should be stripped from the markup.
             *
             * <p>
             * Be aware that if Wicket tags are <i>not</i> stripped, then this may break CSS rules on some browsers.
             * </p>
             */
            private boolean stripWicketTags = true;

            private boolean suppressSignUp = false;

            private boolean suppressPasswordReset = false;

            /**
             * The pattern used for rendering and parsing timestamps.
             */
            private String timestampPattern = "yyyy-MM-dd HH:mm:ss.SSS";

            /**
             * Whether to show an indicator for a form submit button that it has been clicked.
             *
             * This behaviour is enabled by default, but can be disabled using this flag.
             */
            private boolean useIndicatorForFormSubmit = true;
            /**
             * Whether to show an indicator for a no-arg action button that it has been clicked.
             *
             * This behaviour is enabled by default, but can be disabled using this flag.
             */
            private boolean useIndicatorForNoArgAction = true;

            /**
             * Whether the Wicket source plugin should be enabled; if so, the markup includes links to the Wicket source.
             *
             * <p>
             *     Be aware that this can substantially impact performance.
             * </p>
             */
            private boolean wicketSourcePlugin = false;

            private final BookmarkedPages bookmarkedPages = new BookmarkedPages();
            @Data
            public static class BookmarkedPages {

                /**
                 * Determines whether the bookmarks should be available in the header.
                 */
                private boolean showChooser = true;

                private int maxSize = 15;
            }

            private final Breadcrumbs breadcrumbs = new Breadcrumbs();
            @Data
            public static class Breadcrumbs {
                /**
                 * Determines whether the breadcrumbs should be available in the footer.
                 */
                private boolean showChooser = true;
            }
            private final DatePicker datePicker = new DatePicker();
            @Data
            public static class DatePicker {

                /**
                 * As per http://eonasdan.github.io/bootstrap-datetimepicker/Options/#maxdate, in ISO format (per https://github.com/moment/moment/issues/1407).
                 */
                private String minDate = "1900-01-01T00:00:00.000Z";

                /**
                 * As per http://eonasdan.github.io/bootstrap-datetimepicker/Options/#maxdate, in ISO format (per https://github.com/moment/moment/issues/1407).
                 */
                private String maxDate = "2100-01-01T00:00:00.000Z";
            }

            private final DevelopmentUtilities developmentUtilities = new DevelopmentUtilities();
            @Data
            public static class DevelopmentUtilities {

                /**
                 * Determines whether debug bar and other stuff influenced by <tt>org.apache.wicket.settings.DebugSettings#isDevelopmentUtilitiesEnabled()</tt> is enabled or not.
                 *
                 * <p>
                 *     By default, depends on the mode (prototyping = enabled, server = disabled).  This property acts as an override.
                 * </p>
                 */
                private boolean enable = false;
            }

            private final RememberMe rememberMe = new RememberMe();
            @Data
            public static class RememberMe {
                private boolean suppress = false;
                private String cookieKey = "isisWicketRememberMe";
                private String encryptionKey;
            }

            private final Themes themes = new Themes();
            @Data
            public static class Themes {

// isis.viewer.wicket.themes.showChooser
                /**
                 * A comma separated list of enabled theme names, as defined by https://bootswatch.com.
                 */
                private List<String> enabled = new ArrayList<>();

                /**
                 * The initial theme to use.
                 *
                 * <p>
                 *     Expected to be in the list of {@link #getEnabled()} themes.
                 * </p>
                 */
                private String initial = "Flatly";

                private String provider = "org.apache.isis.viewer.wicket.ui.components.widgets.themepicker.IsisWicketThemeSupportDefault";

                /**
                 * Whether the theme chooser should be available in the footer.
                 */
                private boolean showChooser = false;
            }
            private final WhereAmI whereAmI = new WhereAmI();
            @Data
            public static class WhereAmI {
                private boolean enabled = true;
                private int maxParentChainLength = 64;
            }
        }
    }

    private final Viewers viewers = new Viewers();
    @Data
    public static class Viewers {

        private final CollectionLayout collectionLayout = new CollectionLayout();
        @Data
        public static class CollectionLayout {
            private DefaultViewConfiguration defaultView = DefaultViewConfiguration.HIDDEN;
        }

        private final Paged paged = new Paged();
        @Data
        public static class Paged {
            private int parented = 12;
            private int standalone = 25;
        }

        private final ParameterLayout parameterLayout = new ParameterLayout();
        @Data
        public static class ParameterLayout implements ConfigPropsForPropertyOrParameterLayout {
            private LabelPosition labelPosition = LabelPosition.NOT_SPECIFIED;
            private LabelPosition label = LabelPosition.NOT_SPECIFIED;
        }
        private final PropertyLayout propertyLayout = new PropertyLayout();
        @Data
        public static class PropertyLayout implements ConfigPropsForPropertyOrParameterLayout {
            private LabelPosition labelPosition = LabelPosition.NOT_SPECIFIED;
            private LabelPosition label = LabelPosition.NOT_SPECIFIED;
        }

    }

    @Component
    @ConfigurationPropertiesBinding
    public static class PatternsConverter implements Converter<String, Map<Pattern, String>> {
        @Override
        public Map<Pattern, String> convert(String source) {
            return toPatternMap(source);
        }

        /**
         * The pattern matches definitions like:
         * <ul>
         * <li>methodNameRegex:value</li>
         * </ul>
         *
         * <p>
         *     Used for associating cssClass and cssClassFa (font awesome icon) values to method pattern names.
         * </p>
         */
        private final static Pattern PATTERN_FOR_COLON_SEPARATED_PAIR = Pattern.compile("(?<methodRegex>[^:]+):(?<value>.+)");

        private static Map<Pattern, String> toPatternMap(String cssClassPatterns) {
            final Map<Pattern,String> valueByPattern = _Maps.newLinkedHashMap();
            if(cssClassPatterns != null) {
                final StringTokenizer regexToCssClasses = new StringTokenizer(cssClassPatterns, ConfigurationConstants.LIST_SEPARATOR);
                final Map<String,String> valueByRegex = _Maps.newLinkedHashMap();
                while (regexToCssClasses.hasMoreTokens()) {
                    String regexToCssClass = regexToCssClasses.nextToken().trim();
                    if (_Strings.isNullOrEmpty(regexToCssClass)) {
                        continue;
                    }
                    final Matcher matcher = PATTERN_FOR_COLON_SEPARATED_PAIR.matcher(regexToCssClass);
                    if(matcher.matches()) {
                        valueByRegex.put(matcher.group("methodRegex"), matcher.group("value"));
                    }
                }
                for (Map.Entry<String, String> entry : valueByRegex.entrySet()) {
                    final String regex = entry.getKey();
                    final String cssClass = entry.getValue();
                    valueByPattern.put(Pattern.compile(regex), cssClass);
                }
            }
            return valueByPattern;
        }

    }


}
