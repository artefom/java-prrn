package backend.processes;

import backend.rasterio.RasterDataset;
import backend.tasks.IRRNTask;
import backend.tasks.ITaskProvider;

import java.util.Random;

class RRNThread implements Runnable {

    public static final Random rand = new Random();

    ITaskProvider task_provider;

    public RRNThread(ITaskProvider task_provider) {
        this.task_provider = task_provider;
    }

    @Override
    public void run() {
        try {

            IRRNTask current_task;

            // Grab tasks until queue is empty!
            while ((current_task = task_provider.grab()) != null) {
                RasterDataset source = current_task.get_source();
                RasterDataset target = current_task.get_target();
                Thread.sleep(Math.round(rand.nextDouble() * 100) + 100);
                task_provider.release(current_task);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
