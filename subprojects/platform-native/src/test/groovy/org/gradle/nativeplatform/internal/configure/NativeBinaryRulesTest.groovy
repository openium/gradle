/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.nativeplatform.internal.configure

import org.gradle.api.Project
import org.gradle.nativeplatform.NativeComponentSpec
import org.gradle.nativeplatform.NativeExecutableFileSpec
import org.gradle.nativeplatform.NativeExecutableSpec
import org.gradle.nativeplatform.NativeInstallationSpec
import org.gradle.nativeplatform.NativeLibrarySpec
import org.gradle.nativeplatform.internal.NativeBinarySpecInternal
import org.gradle.nativeplatform.internal.NativeExecutableBinarySpecInternal
import org.gradle.nativeplatform.internal.SharedLibraryBinarySpecInternal
import org.gradle.nativeplatform.internal.StaticLibraryBinarySpecInternal
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider
import org.gradle.platform.base.internal.BinaryNamingScheme
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.junit.Rule
import spock.lang.Specification

class NativeBinaryRulesTest extends Specification {
    @Rule
    public final TestNameTestDirectoryProvider tmpDir = new TestNameTestDirectoryProvider();

    def project = Mock(Project)

    def namingScheme = Mock(BinaryNamingScheme)
    def toolProvider = Mock(PlatformToolProvider)
    def platform = Mock(NativePlatformInternal)
    def toolChains = Mock(NativeToolChainRegistryInternal) {
        getForPlatform(platform) >> Mock(NativeToolChainInternal) {
            select(platform) >> toolProvider
        }
    }

    def setup() {
        project.buildDir >> tmpDir.testDirectory
    }

    def "test executable"() {
        def executableFile = new NativeExecutableFileSpec();
        def installation = new NativeInstallationSpec();
        def binary = initBinary(NativeExecutableBinarySpecInternal, NativeExecutableSpec)
        binary.executable >> executableFile
        binary.installation >> installation

        when:
        toolProvider.getExecutableName("base_name") >> "exe_name"

        and:
        NativeBinaryRules.assignTools(binary, toolChains, tmpDir.testDirectory)

        then:
        executableFile.file == tmpDir.testDirectory.file("binaries", "output_dir", "exe_name")
        installation.directory == tmpDir.testDirectory.file("install/output_dir")
    }

    def "test shared library"() {
        def binary = initBinary(SharedLibraryBinarySpecInternal, NativeLibrarySpec)

        when:
        toolProvider.getSharedLibraryName("base_name") >> "shared_library_name"
        toolProvider.getSharedLibraryLinkFileName("base_name") >> "shared_library_link_name"

        and:
        NativeBinaryRules.assignTools(binary, toolChains, tmpDir.testDirectory)

        then:
        1 * binary.setSharedLibraryFile(tmpDir.testDirectory.file("binaries", "output_dir", "shared_library_name"))
        1 * binary.setSharedLibraryLinkFile(tmpDir.testDirectory.file("binaries", "output_dir", "shared_library_link_name"))
    }

    def "test static library"() {
        def binary = initBinary(StaticLibraryBinarySpecInternal, NativeLibrarySpec)

        when:
        toolProvider.getStaticLibraryName("base_name") >> "static_library_name"

        and:
        NativeBinaryRules.assignTools(binary, toolChains, tmpDir.testDirectory)

        then:
        1 * binary.setStaticLibraryFile(tmpDir.testDirectory.file("binaries", "output_dir", "static_library_name"))
    }

    private <T extends NativeBinarySpecInternal> T initBinary(Class<T> type, Class<? extends NativeComponentSpec> componentType) {
        def binary = Mock(type)
        def component = Stub(componentType)
        binary.component >> component
        binary.platformToolProvider >> toolProvider
        binary.namingScheme >> namingScheme
        binary.targetPlatform >> platform


        namingScheme.outputDirectoryBase >> "output_dir"
        component.baseName >> "base_name"
        return binary
    }
}
