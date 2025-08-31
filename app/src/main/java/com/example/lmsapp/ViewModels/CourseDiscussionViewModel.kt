package com.example.lmsapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lmsapp.ViewModels.SharedAuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.lmsapp.ui.data.Comment
import com.example.lmsapp.ui.network.LMSRepository

class CourseDiscussionViewModel(
    private val repository: LMSRepository,
    private val sharedAuthViewModel: SharedAuthViewModel
) : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    val token = sharedAuthViewModel.token.value

    fun loadCourseComments(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.getCourseComments(courseId, token)
            result.onSuccess { fetchedComments ->
                _comments.value = fetchedComments
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to load discussions"
            }
            _isLoading.value = false
        }
    }

    /**
     * Creates a new comment, which can be a top-level post or a reply.
     * Retrieves the authentication token from SharedAuthViewModel.
     * @param content The text content of the comment.
     * @param courseId The ID of the course (required for top-level comments).
     * @param parentId The ID of the parent comment (required for replies).
     */
    fun postNewComment(courseId: String, content: String, parentId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val token = sharedAuthViewModel.token.value

            if (token.isBlank()) {
                _errorMessage.value = "Authentication token is missing."
                _isLoading.value = false
                return@launch
            }

            try {
                Log.d(
                    "CourseDiscussionVM",
                    "Posting new comment for courseId: $courseId, content: ${content.take(20)}..."
                )
                // NOW, handle the `Result` object correctly
                val result: Result<Comment> = repository.createComment(
                    token, // Token is the first argument in your repository function
                    content,
                    courseId, // CourseId is the third argument and can be String? for `createComment`
                    parentId // parentId is the fourth argument and can be String?
                )

                result.onSuccess {
                    Log.d("CourseDiscussionVM", "Comment posted successfully. Reloading comments.")
                    loadCourseComments(courseId) // Refresh the list of comments
                }.onFailure { exception ->
                    _errorMessage.value = "Failed to post comment: ${exception.localizedMessage ?: "Unknown error"}"
                    Log.e(
                        "CourseDiscussionVM",
                        "Failed to post comment: ${exception.message}", exception
                    )
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Network error"
                Log.e("CourseDiscussionVM", "Network error posting comment: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }


        /**
         * Updates an existing comment's content.
         * Retrieves the authentication token from SharedAuthViewModel.
         */
        fun updateExistingComment(commentId: String, newContent: String) {
            viewModelScope.launch {
                _isLoading.value = true
                _errorMessage.value = null

                if (false) {
                    _errorMessage.value = "Authentication required to update comment."
                    _isLoading.value = false
                    return@launch
                }

                val result = repository.updateComment(token, commentId, newContent)
                result.onSuccess { updatedComment ->
                    // Update the comment in the state flow, whether it's top-level or a reply
                    _comments.value = _comments.value.map { topLevelComment ->
                        if (topLevelComment.id == updatedComment.id) {
                            // It's the top-level comment that was updated
                            topLevelComment.copy(
                                content = updatedComment.content,
                                updatedAt = updatedComment.updatedAt
                            )
                        } else {
                            // Check if it's a reply within this top-level comment's replies
                            val updatedReplies = topLevelComment.replies?.map { reply ->
                                if (reply.id == updatedComment.id) {
                                    reply.copy(
                                        content = updatedComment.content,
                                        updatedAt = updatedComment.updatedAt
                                    )
                                } else {
                                    reply
                                }
                            }
                            topLevelComment.copy(replies = updatedReplies)
                        }
                    }
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to update comment"
                }
                _isLoading.value = false
            }
        }

        /**
         * Deletes a comment.
         * Retrieves the authentication token from SharedAuthViewModel.
         */
        fun deleteExistingComment(commentId: String) {
            viewModelScope.launch {
                _isLoading.value = true
                _errorMessage.value = null

                if (token == null) {
                    _errorMessage.value = "Authentication required to delete comment."
                    _isLoading.value = false
                    return@launch
                }

                val result = repository.deleteComment(token, commentId)
                result.onSuccess {
                    // Remove the comment from the state flow, whether it's top-level or a reply
                    _comments.value =
                        _comments.value.filter { it.id != commentId }.map { topLevelComment ->
                            // Also remove from replies if it was a reply to this top-level comment
                            topLevelComment.copy(replies = topLevelComment.replies?.filter { it.id != commentId })
                        }
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to delete comment"
                }
                _isLoading.value = false
            }
        }

        /**
         * Helper function to get a specific top-level comment by ID from the current state.
         * Useful for the detail screen to display the main post content.
         */
        fun getTopLevelCommentById(commentId: String): Comment? {
            return _comments.value.find { it.id == commentId }
        }
    }
}