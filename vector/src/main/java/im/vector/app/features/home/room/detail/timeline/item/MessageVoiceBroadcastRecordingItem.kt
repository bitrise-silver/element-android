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

package im.vector.app.features.home.room.detail.timeline.item

import android.widget.ImageButton
import androidx.core.view.isVisible
import com.airbnb.epoxy.EpoxyModelClass
import im.vector.app.R
import im.vector.app.core.epoxy.onClick
import im.vector.app.features.home.room.detail.RoomDetailAction.VoiceBroadcastAction
import im.vector.app.features.voicebroadcast.model.VoiceBroadcastState
import im.vector.app.features.voicebroadcast.recording.VoiceBroadcastRecorder
import im.vector.app.features.voicebroadcast.views.VoiceBroadcastMetadataView

@EpoxyModelClass
abstract class MessageVoiceBroadcastRecordingItem : AbsMessageVoiceBroadcastItem<MessageVoiceBroadcastRecordingItem.Holder>() {

    private var recorderListener: VoiceBroadcastRecorder.Listener? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        bindVoiceBroadcastItem(holder)
    }

    private fun bindVoiceBroadcastItem(holder: Holder) {
        if (recorder != null && recorder?.state != VoiceBroadcastRecorder.State.Idle) {
            recorderListener = object : VoiceBroadcastRecorder.Listener {
                override fun onStateUpdated(state: VoiceBroadcastRecorder.State) {
                    renderRecordingState(holder, state)
                }
            }.also { recorder?.addListener(it) }
        } else {
            renderVoiceBroadcastState(holder)
        }
    }

    override fun renderMetadata(holder: Holder) {
        with(holder) {
            listenersCountMetadata.isVisible = false
            remainingTimeMetadata.isVisible = false
        }
    }

    private fun renderRecordingState(holder: Holder, state: VoiceBroadcastRecorder.State) {
        when (state) {
            VoiceBroadcastRecorder.State.Recording -> renderRecordingState(holder)
            VoiceBroadcastRecorder.State.Paused -> renderPausedState(holder)
            VoiceBroadcastRecorder.State.Idle -> renderStoppedState(holder)
        }
    }

    private fun renderVoiceBroadcastState(holder: Holder) {
        when (voiceBroadcastState) {
            VoiceBroadcastState.STARTED,
            VoiceBroadcastState.RESUMED -> renderRecordingState(holder)
            VoiceBroadcastState.PAUSED -> renderPausedState(holder)
            VoiceBroadcastState.STOPPED,
            null -> renderStoppedState(holder)
        }
    }

    private fun renderRecordingState(holder: Holder) = with(holder) {
        stopRecordButton.isEnabled = true
        recordButton.isEnabled = true

        val drawableColor = colorProvider.getColorFromAttribute(R.attr.vctr_content_secondary)
        val drawable = drawableProvider.getDrawable(R.drawable.ic_play_pause_pause, drawableColor)
        recordButton.setImageDrawable(drawable)
        recordButton.contentDescription = holder.view.resources.getString(R.string.a11y_pause_voice_broadcast_record)
        recordButton.onClick { callback?.onTimelineItemAction(VoiceBroadcastAction.Recording.Pause) }
        stopRecordButton.onClick { callback?.onTimelineItemAction(VoiceBroadcastAction.Recording.Stop) }
    }

    private fun renderPausedState(holder: Holder) = with(holder) {
        stopRecordButton.isEnabled = true
        recordButton.isEnabled = true

        recordButton.setImageResource(R.drawable.ic_recording_dot)
        recordButton.contentDescription = holder.view.resources.getString(R.string.a11y_resume_voice_broadcast_record)
        recordButton.onClick { callback?.onTimelineItemAction(VoiceBroadcastAction.Recording.Resume) }
        stopRecordButton.onClick { callback?.onTimelineItemAction(VoiceBroadcastAction.Recording.Stop) }
    }

    private fun renderStoppedState(holder: Holder) = with(holder) {
        recordButton.isEnabled = false
        stopRecordButton.isEnabled = false
    }

    override fun unbind(holder: Holder) {
        super.unbind(holder)
        recorderListener?.let { recorder?.removeListener(it) }
        recorderListener = null
    }

    override fun getViewStubId() = STUB_ID

    class Holder : AbsMessageVoiceBroadcastItem.Holder(STUB_ID) {
        val listenersCountMetadata by bind<VoiceBroadcastMetadataView>(R.id.listenersCountMetadata)
        val remainingTimeMetadata by bind<VoiceBroadcastMetadataView>(R.id.remainingTimeMetadata)
        val recordButton by bind<ImageButton>(R.id.recordButton)
        val stopRecordButton by bind<ImageButton>(R.id.stopRecordButton)
    }

    companion object {
        private val STUB_ID = R.id.messageVoiceBroadcastRecordingStub
    }
}
