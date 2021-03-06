/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.forms.adf.engine.shared.formGeneration.processing.fields.fieldInitializers;

import javax.enterprise.context.Dependent;

import org.kie.workbench.common.forms.adf.engine.shared.formGeneration.FormGenerationContext;
import org.kie.workbench.common.forms.adf.engine.shared.formGeneration.processing.fields.FieldInitializer;
import org.kie.workbench.common.forms.adf.service.definitions.elements.FieldElement;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.HasPlaceHolder;
import org.kie.workbench.common.forms.model.FieldDefinition;

@Dependent
public class HasPlaceHolderFieldInitializer implements FieldInitializer {

    @Override
    public boolean supports(FieldDefinition fieldDefinition) {
        return fieldDefinition instanceof HasPlaceHolder;
    }

    @Override
    public void initialize(FieldDefinition fieldDefinition,
                           FieldElement fieldElement,
                           FormGenerationContext context) {
        String placeHolder = fieldElement.getParams().get("placeHolder");

        if (placeHolder != null) {
            String placeHolderValue = context.getI18nHelper().getTranslation(placeHolder);
            if (placeHolderValue != null) {
                ((HasPlaceHolder) fieldDefinition).setPlaceHolder(placeHolderValue);
            }
        }
    }
}
