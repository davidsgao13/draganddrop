package com.example.draganddrop.presentation

import android.content.ClipData
import android.content.ClipDescription
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DragAndDropBoxes(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        val boxCount = 5
        // Use property delegation with 'by' for a state
        var dragBoxIndex by remember {
            mutableIntStateOf(0)
        }
        // Doesn't need 'by' for property delegation because colors is not a state
        val colors = remember {
            (1..boxCount).map {
                // Generate a random color
                Color(Random.nextLong()).copy(alpha = 1f)
            }
        }
        repeat(boxCount) { index ->
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(colors[index])
                .dragAndDropTarget(shouldStartDragAndDrop = { event ->
                    // We specify this as PlainText because we're going to be dragging and dropping
                    // our Text() composable, and the Text() composable we're dropping into is
                    // expecting PlainText. Thus we need to align on the event.mimeTypes() so that
                    // we only accept events that are PlainText. Any other composable inside the Box
                    // composable that is not a PlainText will not be considered for drag and drop
                    // functionality
                    event.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                },
                    // Declared with remember so we don't need to redeclare it with every
                    // recomposition. Always use remember for stable and non-changing objects,
                    // that don't need to change across recompositions. Stateless computations
                    // don't need to be recomposed; Compose functions should be as stateless
                    // and declarative as possible. Use remember to offload persistent state
                    // management to the memory system. Objects that are only created once during
                    // initialization should be remembered so that we can reuse the instance during
                    // subsequent recompositions
                    target = remember {
                        object: DragAndDropTarget {
                            override fun onDrop(event: DragAndDropEvent): Boolean {
                                // This is invoked if the Box is dragged into is actually PlainText
                                // to perform the move operation (onDrop)
                                val text = event.toAndroidDragEvent()
                                    .clipData?.getItemAt(0)?.text
                                println("Drag data was $text")
                                dragBoxIndex = index
                                return true
                            }
                        }
                    }),
                contentAlignment = Alignment.Center
            ) {
                // Create a small animation when dragging and dropping
                // Use AnimatedVisibility for any composable animations
                // We need to specify this@Column because of scope ambiguity; we're inside a Box
                // Composable inside a Column composable; we need to declare the scope of this
                // visibility to the Column layer
                this@Column.AnimatedVisibility(
                    // Only show this animation when the index of the box is equal to the
                    // dragBoxIndex, meaning the index of the box we're drawing is the same as the
                    // box we've declared as our current dragBoxIndex
                    visible = index == dragBoxIndex,
                    // Adds these animations to the fade in and fade out
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Text(text = "Drag me!",
                        fontSize = 40.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        // We need to modify this Text with a dragAndDropSource, because this is
                        // the composable we're going to be dragging and dropping
                        modifier = Modifier.dragAndDropSource {
                            detectTapGestures(
                                // Only allow dragging when a tap gesture is a long press; we don't
                                // want to allow drag and drop if the user accidentally taps on the
                                // Text, we want to make it obvious
                                onLongPress = { offset ->
                                    /**
                                     * With every drag and drop action, we need to attach a
                                     * @ClipData to that action. For example, command + C is clip
                                     * data; it essentially stores the information of what is
                                     * copied. We need to ensure that wherever the location is that
                                     * we're pasting our copy, that the paste location should
                                     * actually be able to take in whatever was copied. For example,
                                     * we don't want to copy a paragraph of plain text, then paste
                                     * that plain text in our file system and see plain text in our
                                     * file system (that would be really, really bad). We need
                                     * to make sure that the location we are pasting the copied text
                                     * or resource into can actually hold it. For example, if we
                                     * copied a file and pasted it into a directory, then that
                                     * should work, since the directory expects a file. A Word
                                     * document is not intended to hold a file, so we don't want the
                                     * location (Word) to hold the object (file).
                                     */
                                    startTransfer(
                                        // This is commonly used for drag and drop for file system
                                        // UIs -- very useful!
                                        transferData = DragAndDropTransferData(
                                            // This is where we specify that we're looking for
                                            // PlainText ClipData
                                            clipData = ClipData.newPlainText(
                                                "text",
                                                "Drag me!"
                                            )
                                        )
                                    )
                                }
                            )
                        })
                }
            }
        }
    }
}