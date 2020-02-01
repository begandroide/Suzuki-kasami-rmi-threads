import java.rmi.RemoteException;

import RMI.Suzuki_kasami_rmi;

public class ProcessThread implements Runnable {

        public Suzuki_kasami_rmi process;

        public ProcessThread(Suzuki_kasami_rmi process) {
                this.process = process;
        }

        @Override
        public void run() {
                try {
                        process.initializeExtractProcess();
                } catch (RemoteException e) {
                        throw new RuntimeException(e);
                }
        }
}
