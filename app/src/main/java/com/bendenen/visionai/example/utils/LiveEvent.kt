package com.bendenen.visionai.example.utils

/**
 * Wrapper class for data exposed via LiveData that represents an event.
 *
 * Example usage:
 * ```
 * class MyViewModel : ViewModel() {
 *
 *      val myEventField = MutableLiveData<LiveEvent<Int>>()
 *
 *      fun applyEvent(myNumber: Int) = myEventField.value = LiveEvent(myNumber)
 * }
 *
 *  class MyActivity : AppCompatActivity() {
 *
 *      // some initialization code
 *
 *      val viewModel = ViewModelProviders.of(this).get(MyViewModel::class.java)
 *      viewModel.myEventField.observe(this, myEvent())
 *
 *      // standard observer
 *      private fun myEvent() = Observer<LiveEvent<Int>> {
 *          val number = it?.getContentIfNotHandled()
 *          when (number) {
 *              null -> // already handled
 *              // ...
 *          }
 *      }
 *
 *      // better: LiveEventObserver
 *      private fun myLiveEvent() = LiveEventObserver<Int> {
 *          when (it) {
 *              // ...
 *          }
 *      }
 * ```
 */
open class LiveEvent<out T>(private val content: T) {

    var isHandled = false
        private set

    /**
     * Returns the content of this event if called for the first time, returns null
     * on every following call.
     */
    fun getContentIfNotHandled(): T? =
        if (isHandled) {
            null
        } else {
            isHandled = true
            content
        }

    /**
     * Always returns the content.
     */
    fun peekContent(): T = content
}