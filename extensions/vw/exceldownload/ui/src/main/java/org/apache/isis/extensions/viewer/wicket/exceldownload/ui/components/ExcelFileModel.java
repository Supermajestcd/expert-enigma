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
package org.apache.isis.extensions.viewer.wicket.exceldownload.ui.components;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.wicket.model.LoadableDetachableModel;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;

import lombok.val;

class ExcelFileModel extends LoadableDetachableModel<File> {

    private static final long serialVersionUID = 1L;

    private final EntityCollectionModel model;

    public ExcelFileModel(EntityCollectionModel model) {
        this.model = model;
    }

    static class RowFactory {
        private final Sheet sheet;
        private int rowNum;

        RowFactory(Sheet sheet) {
            this.sheet = sheet;
        }

        public Row newRow() {
            return sheet.createRow((short) rowNum++);
        }
    }

    @Override
    protected File load() {

        try {
            return createFile();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File createFile() throws IOException, FileNotFoundException {
        try(final Workbook wb = new XSSFWorkbook()) {
            String sheetName = model.getName();
            if(sheetName==null||sheetName.length()==0) sheetName = "Collection";
            final File tempFile = File.createTempFile(ExcelFileModel.class.getCanonicalName(), sheetName + ".xlsx");
            
            try(final FileOutputStream fos = new FileOutputStream(tempFile)) {
                final Sheet sheet = wb.createSheet(sheetName);
        
                final ObjectSpecification typeOfSpec = model.getTypeOfSpecification();
        
        
                //XXX legacy of 1.15.1
                //            @SuppressWarnings("unchecked")
                //            final Filter<ObjectAssociation> filter = Filters.and(
                //                    ObjectAssociationFilters.PROPERTIES, 
                //                    ObjectAssociationFilters.staticallyVisible(model.isParented()? Where.PARENTED_TABLES: Where.STANDALONE_TABLES));
                //            
                final Predicate<ObjectAssociation> filter = oa->{
                    if(!oa.isPropertyOrCollection())
                        return false;
                    //            	if(model.getEntityModel()!=null && model.getEntityModel().getObject()!=null &&
                    //            		oa.isVisible(
                    //            			model.getEntityModel().getObject(), 
                    //            			InteractionInitiatedBy.USER, 
                    //            			model.isParented()? Where.PARENTED_TABLES: Where.STANDALONE_TABLES).isVetoed() )
                    //            			return false;
                    return true;
                };            
        
                final List<? extends ObjectAssociation> propertyList = typeOfSpec
                        .streamAssociations(Contributed.INCLUDED)
                        .filter(filter)
                        .collect(Collectors.toList());
        
                final ExcelFileModel.RowFactory rowFactory = new RowFactory(sheet);
                Row row = rowFactory.newRow();
        
        
                // header row
                int i=0;
                for (ObjectAssociation property : propertyList) {
                    final Cell cell = row.createCell((short) i++);
                    cell.setCellValue(property.getName());
                }
        
                final CellStyle dateCellStyle = createDateFormatCellStyle(wb);
        
                // detail rows
                final List<ManagedObject> adapters = model.getObject();
                for (val objectAdapter : adapters) {
                    row = rowFactory.newRow();
                    i=0;
                    for (final ObjectAssociation property : propertyList) {
                        final Cell cell = row.createCell((short) i++);
                        setCellValue(objectAdapter, property, cell, dateCellStyle);
                    }
                }
        
                // freeze panes
                sheet.createFreezePane(0, 1);
        
                wb.write(fos);
    
            }
            return tempFile;
        }
    }

    protected void autoSize(final Sheet sh, int numProps) {
        for(int prop=0; prop<numProps; prop++) {
            sh.autoSizeColumn(prop);
        }
    }

    protected CellStyle createDateFormatCellStyle(final Workbook wb) {
        CreationHelper createHelper = wb.getCreationHelper();
        short dateFormat = createHelper.createDataFormat().getFormat("yyyy-mm-dd");
        CellStyle dateCellStyle = wb.createCellStyle();
        dateCellStyle.setDataFormat(dateFormat);
        return dateCellStyle;
    }

    private void setCellValue(
            final ManagedObject objectAdapter, 
            final ObjectAssociation property, 
            final Cell cell, 
            CellStyle dateCellStyle) {

        val valueAdapter = property.get(objectAdapter);
        val valueAsObj = valueAdapter!=null ? valueAdapter.getPojo() : null;

        // null
        if(valueAsObj == null) {
            cell.setBlank();
            return;
        }

        // boolean
        if(valueAsObj instanceof Boolean) { 
            boolean value = (Boolean) valueAsObj;
            cell.setCellValue(value);
            return;
        } 

        // date
        if(valueAsObj instanceof Date) {
            Date value = (Date) valueAsObj;
            setCellValueForDate(cell, value, dateCellStyle);
            return;
        } 
        if(valueAsObj instanceof LocalDate) {
            LocalDate value = (LocalDate) valueAsObj;
            Date date = Util_TimeConversion.toDate(value); 
            setCellValueForDate(cell, date, dateCellStyle);
            return;
        } 
        if(valueAsObj instanceof LocalDateTime) {
            LocalDateTime value = (LocalDateTime) valueAsObj;
            Date date = Util_TimeConversion.toDate(value);
            setCellValueForDate(cell, date, dateCellStyle);
            return;
        } 
        if(valueAsObj instanceof OffsetDateTime) {
            OffsetDateTime value = (OffsetDateTime) valueAsObj;
            Date date = Util_TimeConversion.toDate(value); 
            setCellValueForDate(cell, date, dateCellStyle);
            return;
        }

        // number
        if(valueAsObj instanceof Double) {
            Double value = (Double) valueAsObj;
            setCellValueForDouble(cell, (double)value);
            return;
        }
        if(valueAsObj instanceof Float) {
            Float value = (Float) valueAsObj;
            setCellValueForDouble(cell, (double)value);
            return;
        } 
        if(valueAsObj instanceof BigDecimal) {
            BigDecimal value = (BigDecimal) valueAsObj;
            setCellValueForDouble(cell, value.doubleValue());
            return;
        } 
        if(valueAsObj instanceof BigInteger) {
            BigInteger value = (BigInteger) valueAsObj;
            setCellValueForDouble(cell, value.doubleValue());
            return;
        } 
        if(valueAsObj instanceof Long) {
            Long value = (Long) valueAsObj;
            setCellValueForDouble(cell, (double)value);
            return;
        } 
        if(valueAsObj instanceof Integer) {
            Integer value = (Integer) valueAsObj;
            setCellValueForDouble(cell, (double)value);
            return;
        } 
        if(valueAsObj instanceof Short) {
            Short value = (Short) valueAsObj;
            setCellValueForDouble(cell, (double)value);
            return;
        } 
        if(valueAsObj instanceof Byte) {
            Byte value = (Byte) valueAsObj;
            setCellValueForDouble(cell, (double)value);
            return;
        } 

        final String objectAsStr = valueAdapter.titleString(null);
        cell.setCellValue(objectAsStr);
        return;
    }

    private static void setCellValueForDouble(final Cell cell, double value2) {
        cell.setCellValue(value2);
    }

    private static void setCellValueForDate(final Cell cell, Date date, CellStyle dateCellStyle) {
        cell.setCellValue(date);
        cell.setCellStyle(dateCellStyle);
    }
}