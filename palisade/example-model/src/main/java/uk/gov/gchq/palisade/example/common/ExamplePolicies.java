/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.example.common;

import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.example.rule.AddressRule;
import uk.gov.gchq.palisade.example.rule.ContactsRule;
import uk.gov.gchq.palisade.example.rule.DutyOfCareRule;
import uk.gov.gchq.palisade.example.rule.HireDateRule;
import uk.gov.gchq.palisade.example.rule.IdentityRule;
import uk.gov.gchq.palisade.example.rule.OrgDetailsRule;
import uk.gov.gchq.palisade.example.rule.PersonalCharacteristicsRule;
import uk.gov.gchq.palisade.example.rule.ProtectedCharacteristicsRule;
import uk.gov.gchq.palisade.example.rule.WorkLocationRule;
import uk.gov.gchq.palisade.example.util.ExampleFileUtil;
import uk.gov.gchq.palisade.policy.service.Policy;
import uk.gov.gchq.palisade.policy.service.request.SetResourcePolicyRequest;
import uk.gov.gchq.palisade.resource.ParentResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.StreamSupport;

import static java.util.Objects.isNull;

/**
 * Convenience methods for creating example policies.
 */
public final class ExamplePolicies {

    private ExamplePolicies() {
    }

    /**
     * Create an example security policy on the given file.
     *
     * @param file file to set policy for
     * @return security policy request
     */
    public static SetResourcePolicyRequest getExamplePolicy(final String file) {

        Resource resource;
        if (file.endsWith(".avro")) {
            resource = new FileResource().id(file).type(Employee.class.getTypeName()).serialisedFormat("avro").parent(getParent(file));
        } else {
            resource = new DirectoryResource().id(file).parent(getParent(file));
        }

        return new SetResourcePolicyRequest()
                .resource(resource)
                .policy(new Policy<Employee>()
                        .owner(ExampleUsers.getFaithBroker())
                        .recordLevelRule(
                                "1-Personal characteristics Rule",
                                new PersonalCharacteristicsRule()
                        )
                        .recordLevelRule(
                                "2-Protected characteristics rule",
                                new ProtectedCharacteristicsRule()
                        )
                        .recordLevelRule(
                                "3-Organisation details rule",
                                new OrgDetailsRule()
                        )
                        .recordLevelRule(
                                "4-Identity rule",
                                new IdentityRule()
                        )
                        .recordLevelRule(
                                "5-Hire date rule",
                                new HireDateRule()
                        )
                        .recordLevelRule(
                                "6-Contact details rule",
                                new ContactsRule()
                        )
                        .recordLevelRule(
                                "7-Address rule",
                                new AddressRule()
                        )
                        .recordLevelRule(
                                "8-Work location rule",
                                new WorkLocationRule()
                        )
                );
    }

    /**
     * Create an example security policy that contains no record level rules.
     *
     * @param file the file to set policy for
     * @return security policy request
     */
    public static SetResourcePolicyRequest getEmptyPolicy(final String file) {
        return new SetResourcePolicyRequest()
                .resource(new FileResource().id(file).type(Employee.class.getTypeName()).serialisedFormat("avro").parent(getParent(file)))
                .policy(new Policy<Employee>()
                        .owner(ExampleUsers.getFaithBroker())
                );
    }

    public static ParentResource getParent(final String fileURL) {
        URI normalised = ExampleFileUtil.convertToFileURI(fileURL);
        //this should only be applied to URLs that start with 'file://' not other types of URL
        if (normalised.getScheme().equals(FileSystems.getDefault().provider().getScheme())) {
            Path current = Paths.get(normalised);
            Path parent = current.getParent();
            //no parent can be found, must already be a directory tree root
            if (isNull(parent)) {
                throw new IllegalArgumentException(fileURL + " is already a directory tree root");
            } else if (isDirectoryRoot(parent)) {
                //else if this is a directory tree root
                return new SystemResource().id(parent.toUri().toString());
            } else {
                //else recurse up a level
                return new DirectoryResource().id(parent.toUri().toString()).parent(getParent(parent.toUri().toString()));
            }
        } else {
            //if this is another scheme then there is no definable parent
            return new SystemResource().id("");
        }
    }

    /**
     * Tests if the given {@link Path} represents a root of the default local file system.
     *
     * @param path the path to test
     * @return true if {@code parent} is a root
     */
    public static boolean isDirectoryRoot(final Path path) {
        return StreamSupport
                .stream(FileSystems.getDefault()
                        .getRootDirectories()
                        .spliterator(), false)
                .anyMatch(path::equals);
    }
}
