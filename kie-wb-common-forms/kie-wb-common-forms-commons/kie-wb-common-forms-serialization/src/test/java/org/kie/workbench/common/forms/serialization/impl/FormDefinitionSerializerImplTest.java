/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.forms.serialization.impl;

import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.BasicTypeFieldProvider;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.selectors.listBox.definition.EnumListBoxFieldDefinition;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.multipleSubform.definition.MultipleSubFormFieldDefinition;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.relations.subForm.definition.SubFormFieldDefinition;
import org.kie.workbench.common.forms.fields.test.TestFieldManager;
import org.kie.workbench.common.forms.model.FieldDataType;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.kie.workbench.common.forms.model.FormDefinition;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.ext.layout.editor.api.editor.LayoutTemplate;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class FormDefinitionSerializerImplTest extends TestCase {

    private TestFieldManager fieldManager;

    private FormDefinitionSerializerImpl definitionSerializer;

    private FormDefinition formDefinition;

    @Before
    public void initTest() {
        fieldManager = new TestFieldManager();

        definitionSerializer = new FormDefinitionSerializerImpl(new FieldSerializer(),
                                                                new FormModelSerializer());

        formDefinition = new FormDefinition();
        formDefinition.setId("testForm");
        formDefinition.setName("testForm");

        formDefinition.setLayoutTemplate(new LayoutTemplate());

        for (BasicTypeFieldProvider provider : fieldManager.getAllBasicTypeProviders()) {
            for (String type : provider.getSupportedTypes()) {
                FieldDefinition field = provider.getFieldByType(new FieldDataType(type));

                assertNotNull(field);

                String fieldDescription = provider.getFieldTypeName() + "_" + type;

                field.setName(fieldDescription);
                field.setLabel(fieldDescription);

                field.setStandaloneClassName(type);

                field.setBinding(fieldDescription);

                formDefinition.getFields().add(field);
            }
        }

        SubFormFieldDefinition subForm = new SubFormFieldDefinition();

        subForm.setLabel("SubForm");
        subForm.setNestedForm("");
        subForm.setBinding("model");
        subForm.setStandaloneClassName("org.test.MyTestModel");
        subForm.setBinding("SubForm");

        formDefinition.getFields().add(subForm);

        MultipleSubFormFieldDefinition multipleSubForm = new MultipleSubFormFieldDefinition();

        multipleSubForm.setLabel("MultipleSubForm");
        multipleSubForm.setCreationForm("");
        multipleSubForm.setEditionForm("");
        multipleSubForm.setStandaloneClassName("org.test.MyTestModel");
        multipleSubForm.setBinding("MultipleSubForm");

        formDefinition.getFields().add(multipleSubForm);

        EnumListBoxFieldDefinition enumListBox = new EnumListBoxFieldDefinition();

        enumListBox.setLabel("EnumListBox");
        enumListBox.setBinding("EnumListBox");
        enumListBox.setStandaloneClassName("org.test.MyTestModel");

        formDefinition.getFields().add(enumListBox);
    }

    @Test
    public void testFormSerialization() {
        doSerializationTest();
    }

    protected String doSerializationTest() {
        String serializedForm = definitionSerializer.serialize(formDefinition);

        assertEquals(formDefinition.getFields().size(),
                     StringUtils.countMatches(serializedForm,
                                              "\"code\""));
        assertNotNull(serializedForm);
        assertNotEquals(0,
                        serializedForm.length());

        return serializedForm;
    }

    @Test
    public void testFormDeSerialization() {
        String serializedForm = doSerializationTest();

        FormDefinition deSerializedForm = definitionSerializer.deserialize(serializedForm);

        assertNotNull(deSerializedForm);

        assertEquals(formDefinition.getFields().size(),
                     deSerializedForm.getFields().size());

        for (FieldDefinition originalField : formDefinition.getFields()) {
            FieldDefinition resultField = deSerializedForm.getFieldById(originalField.getId());

            assertNotNull(resultField);
            assertEquals(originalField.getClass(),
                         resultField.getClass());
            assertEquals(originalField.getName(),
                         resultField.getName());
            assertEquals(originalField.getLabel(),
                         resultField.getLabel());
            assertEquals(originalField.getStandaloneClassName(),
                         resultField.getStandaloneClassName());
        }
    }
}
