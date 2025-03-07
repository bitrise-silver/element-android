/*
 * Copyright (c) 2022 New Vector Ltd
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

package im.vector.app.features.settings.devices.v2.notification

import im.vector.app.core.di.ActiveSessionHolder
import org.matrix.android.sdk.api.session.accountdata.UserAccountDataTypes
import javax.inject.Inject

class CheckIfCanTogglePushNotificationsViaAccountDataUseCase @Inject constructor(
        private val activeSessionHolder: ActiveSessionHolder,
) {

    fun execute(deviceId: String): Boolean {
        return activeSessionHolder
                .getSafeActiveSession()
                ?.accountDataService()
                ?.getUserAccountDataEvent(UserAccountDataTypes.TYPE_LOCAL_NOTIFICATION_SETTINGS + deviceId) != null
    }
}
