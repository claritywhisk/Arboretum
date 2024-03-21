package asterhaven.vega.arboretum.utility

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

class SingleRestartableJob(private val dispatcher : CoroutineDispatcher = Dispatchers.Default) {
    private var job : Job? = null
    fun resetThenLaunch(suspendCodeBlock: suspend () -> Unit){
        CoroutineScope(dispatcher).launch {
            job?.cancelAndJoin()
            synchronized(this@SingleRestartableJob) {
                if (job?.isCompleted != false) job = CoroutineScope(dispatcher).launch {
                    suspendCodeBlock()
                }
            }
        }
    }
}