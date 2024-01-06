package fr.pcm.projet.projet_pcm

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class RappelWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams){
    override fun doWork(): Result {
        createNotif(context)
        return Result.success()
    }
}