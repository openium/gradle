/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.model.internal.inspect;

import net.jcip.annotations.ThreadSafe;
import org.gradle.model.InvalidModelRuleDeclarationException;
import org.gradle.model.internal.core.ModelPath;

import java.lang.annotation.Annotation;

@ThreadSafe
public abstract class AbstractModelCreationRuleExtractor<T extends Annotation> extends AbstractAnnotationDrivenModelRuleExtractor<T> {

    protected String determineModelName(MethodRuleDefinition<?, ?> ruleDefinition) {
        String annotationValue = getNameFromAnnotation(ruleDefinition);
        String modelName = (annotationValue == null || annotationValue.isEmpty()) ? ruleDefinition.getMethodName() : annotationValue;

        try {
            ModelPath.validatePath(modelName);
        } catch (Exception e) {
            throw new InvalidModelRuleDeclarationException(String.format("Path of declared model element created by rule %s is invalid.", ruleDefinition.getDescriptor()), e);
        }

        return modelName;
    }

    protected abstract String getNameFromAnnotation(MethodRuleDefinition<?, ?> ruleDefinition);
}
