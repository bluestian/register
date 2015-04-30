/*
 * Copyright 2015 openregister.org
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

package uk.gov.openregister.validation;

import java.util.List;

public class ValidationResult {

    List<String> invalidKeys;
    List<String> missingKeys;

    public ValidationResult(List<String> invalidKeys, List<String> missingKeys) {
        this.invalidKeys = invalidKeys;
        this.missingKeys = missingKeys;
    }

    public List<String> getInvalidKeys() {
        return invalidKeys;
    }

    public List<String> getMissingKeys() {
        return missingKeys;
    }

    public boolean isValid() {
        return invalidKeys.isEmpty() && missingKeys.isEmpty();
    }
}
