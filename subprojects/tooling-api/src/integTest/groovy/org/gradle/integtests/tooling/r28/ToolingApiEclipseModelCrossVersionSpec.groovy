/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.integtests.tooling.r28

import org.gradle.integtests.tooling.fixture.TargetGradleVersion
import org.gradle.integtests.tooling.fixture.ToolingApiSpecification
import org.gradle.tooling.model.eclipse.EclipseProject

class ToolingApiEclipseModelCrossVersionSpec extends ToolingApiSpecification {

    @TargetGradleVersion(">=2.8")
    def "makes sure module names are unique in gradle"() {
        given:
        file('build.gradle').text = """
subprojects {
    apply plugin: 'java'
}

project(':impl') {
    dependencies {
        compile project(':api')
    }
}

project(':contrib:impl') {
    dependencies {
        compile project(':contrib:api')
    }
}
"""
        file('settings.gradle').text = """
        rootProject.name = "root"
        include 'api', 'impl', 'contrib:api', 'contrib:impl'"""

        when:
        EclipseProject rootProject = withConnection { connection -> connection.getModel(EclipseProject.class) }
        EclipseProject rootImplProject = rootProject.children.find { it.name == 'root-impl' }
        EclipseProject contribProject = rootProject.children.find { it.name == 'contrib' }
        EclipseProject contribImplProject = contribProject.children.find { it.name == 'contrib-impl' }
        EclipseProject rootApiProject = rootProject.children.find { it.name == 'root-api' }
        EclipseProject contribApiProject = contribProject.children.find { it.name == 'contrib-api' }

        then:
        contribImplProject.projectDependencies.any { it.path == 'contrib-api' && it.targetProject == contribApiProject }
        rootImplProject.projectDependencies.any { it.path == 'root-api' && it.targetProject == rootApiProject }

    }


}
