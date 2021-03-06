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

package org.kie.workbench.common.stunner.bpmn.definition.property.task;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Before;
import org.junit.Test;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagram;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.DiagramSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.Id;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.Package;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.Version;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.Name;

import static org.junit.Assert.*;

public class BPMNDiagramTest {

    private Validator validator;

    private static final String NAME_VALID = "My New BP";
    private static final String NAME_INVALID = "";

    private static final String ID_VALID = "Project1.MyNewBP";
    private static final String ID_INVALID = "";

    private static final String PACKAGE_VALID = "myorg.project1";
    private static final String PACKAGE_INVALID = "";

    private static final String VERSION_VALID = "1.0";
    private static final String VERSION_INVALID = "";

    @Before
    public void init() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        this.validator = vf.getValidator();
    }

    public BPMNDiagram createValidBpmnDiagram() {
        BPMNDiagram bpmnDiagram = new BPMNDiagram.BPMNDiagramBuilder().build();
        DiagramSet diagramSet = bpmnDiagram.getDiagramSet();
        diagramSet.setName(new Name(NAME_VALID));
        diagramSet.setId(new Id(ID_VALID));
        diagramSet.setPackageProperty(new Package(PACKAGE_VALID));
        diagramSet.setVersion(new Version(VERSION_VALID));

        return bpmnDiagram;
    }

    @Test
    public void testAllValid() {
        BPMNDiagram bpmnDiagram = createValidBpmnDiagram();
        Set<ConstraintViolation<BPMNDiagram>> violations = this.validator.validate(bpmnDiagram);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testNameInvalid() {
        BPMNDiagram bpmnDiagram = createValidBpmnDiagram();
        bpmnDiagram.getDiagramSet().setName(new Name(NAME_INVALID));
        Set<ConstraintViolation<BPMNDiagram>> violations = this.validator.validate(bpmnDiagram);
        assertEquals(1,
                     violations.size());
    }

    @Test
    public void testIDInvalid() {
        BPMNDiagram bpmnDiagram = createValidBpmnDiagram();
        bpmnDiagram.getDiagramSet().setId(new Id(ID_INVALID));
        Set<ConstraintViolation<BPMNDiagram>> violations = this.validator.validate(bpmnDiagram);
        assertEquals(1,
                     violations.size());
    }

    @Test
    public void testPackageInvalid() {
        BPMNDiagram bpmnDiagram = createValidBpmnDiagram();
        bpmnDiagram.getDiagramSet().setPackageProperty(new Package(PACKAGE_INVALID));
        Set<ConstraintViolation<BPMNDiagram>> violations = this.validator.validate(bpmnDiagram);
        assertEquals(1,
                     violations.size());
    }

    @Test
    public void testVersionInvalid() {
        BPMNDiagram bpmnDiagram = createValidBpmnDiagram();
        bpmnDiagram.getDiagramSet().setVersion(new Version(VERSION_INVALID));
        Set<ConstraintViolation<BPMNDiagram>> violations = this.validator.validate(bpmnDiagram);
        assertEquals(1,
                     violations.size());
    }
}
