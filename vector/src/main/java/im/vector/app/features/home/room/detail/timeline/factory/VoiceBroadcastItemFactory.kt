/*
 * Copyright 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package im.vector.app.features.home.room.detail.timeline.factory

import im.vector.app.core.resources.ColorProvider
import im.vector.app.core.resources.DrawableProvider
import im.vector.app.features.displayname.getBestName
import im.vector.app.features.home.room.detail.timeline.helper.AvatarSizeProvider
import im.vector.app.features.home.room.detail.timeline.helper.VoiceBroadcastEventsGroup
import im.vector.app.features.home.room.detail.timeline.item.AbsMessageItem
import im.vector.app.features.home.room.detail.timeline.item.AbsMessageVoiceBroadcastItem
import im.vector.app.features.home.room.detail.timeline.item.MessageVoiceBroadcastListeningItem
import im.vector.app.features.home.room.detail.timeline.item.MessageVoiceBroadcastListeningItem_
import im.vector.app.features.home.room.detail.timeline.item.MessageVoiceBroadcastRecordingItem
import im.vector.app.features.home.room.detail.timeline.item.MessageVoiceBroadcastRecordingItem_
import im.vector.app.features.voicebroadcast.listening.VoiceBroadcastPlayer
import im.vector.app.features.voicebroadcast.model.MessageVoiceBroadcastInfoContent
import im.vector.app.features.voicebroadcast.model.VoiceBroadcastState
import im.vector.app.features.voicebroadcast.model.asVoiceBroadcastEvent
import im.vector.app.features.voicebroadcast.recording.VoiceBroadcastRecorder
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.getRoom
import org.matrix.android.sdk.api.session.getUserOrDefault
import org.matrix.android.sdk.api.util.toMatrixItem
import javax.inject.Inject

class VoiceBroadcastItemFactory @Inject constructor(
        private val session: Session,
        private val avatarSizeProvider: AvatarSizeProvider,
        private val colorProvider: ColorProvider,
        private val drawableProvider: DrawableProvider,
        private val voiceBroadcastRecorder: VoiceBroadcastRecorder?,
        private val voiceBroadcastPlayer: VoiceBroadcastPlayer,
) {

    fun create(
            params: TimelineItemFactoryParams,
            messageContent: MessageVoiceBroadcastInfoContent,
            highlight: Boolean,
            attributes: AbsMessageItem.Attributes,
    ): AbsMessageVoiceBroadcastItem<*>? {
        // Only display item of the initial event with updated data
        if (messageContent.voiceBroadcastState != VoiceBroadcastState.STARTED) return null

        val voiceBroadcastEventsGroup = params.eventsGroup?.let { VoiceBroadcastEventsGroup(it) } ?: return null
        val voiceBroadcastEvent = voiceBroadcastEventsGroup.getLastDisplayableEvent().root.asVoiceBroadcastEvent() ?: return null
        val voiceBroadcastContent = voiceBroadcastEvent.content ?: return null
        val voiceBroadcastId = voiceBroadcastEventsGroup.voiceBroadcastId

        val isRecording = voiceBroadcastContent.voiceBroadcastState != VoiceBroadcastState.STOPPED &&
                voiceBroadcastEvent.root.stateKey == session.myUserId &&
                messageContent.deviceId == session.sessionParams.deviceId

        val voiceBroadcastAttributes = AbsMessageVoiceBroadcastItem.Attributes(
                voiceBroadcastId = voiceBroadcastId,
                voiceBroadcastState = voiceBroadcastContent.voiceBroadcastState,
                duration = voiceBroadcastEventsGroup.getDuration(),
                recorderName = params.event.root.stateKey?.let { session.getUserOrDefault(it) }?.toMatrixItem()?.getBestName().orEmpty(),
                recorder = voiceBroadcastRecorder,
                player = voiceBroadcastPlayer,
                roomItem = session.getRoom(params.event.roomId)?.roomSummary()?.toMatrixItem(),
                colorProvider = colorProvider,
                drawableProvider = drawableProvider,
        )

        return if (isRecording) {
            createRecordingItem(highlight, attributes, voiceBroadcastAttributes)
        } else {
            createListeningItem(highlight, attributes, voiceBroadcastAttributes)
        }
    }

    private fun createRecordingItem(
            highlight: Boolean,
            attributes: AbsMessageItem.Attributes,
            voiceBroadcastAttributes: AbsMessageVoiceBroadcastItem.Attributes,
    ): MessageVoiceBroadcastRecordingItem {
        return MessageVoiceBroadcastRecordingItem_()
                .id("voice_broadcast_${voiceBroadcastAttributes.voiceBroadcastId}")
                .attributes(attributes)
                .voiceBroadcastAttributes(voiceBroadcastAttributes)
                .highlighted(highlight)
                .leftGuideline(avatarSizeProvider.leftGuideline)
    }

    private fun createListeningItem(
            highlight: Boolean,
            attributes: AbsMessageItem.Attributes,
            voiceBroadcastAttributes: AbsMessageVoiceBroadcastItem.Attributes,
    ): MessageVoiceBroadcastListeningItem {
        return MessageVoiceBroadcastListeningItem_()
                .id("voice_broadcast_${voiceBroadcastAttributes.voiceBroadcastId}")
                .attributes(attributes)
                .voiceBroadcastAttributes(voiceBroadcastAttributes)
                .highlighted(highlight)
                .leftGuideline(avatarSizeProvider.leftGuideline)
    }
}
