package RMI;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import RMI.ProcessState.Status;

public class Suzuki_kasami extends UnicastRemoteObject implements Suzuki_kasami_rmi, Runnable {

        /**
         * A delay between checks of token acquisition.
         */
        private static final int TOKEN_WAIT_DELAY = 10;

        // for every process the sequence number of the last request this process knows
        // about.
        private List<Integer> RN;

        /**
         * Index of a current process
         */
        private int index;

        /**
         * Number of processes participating in message exchange
         */
        private int numProcesses;

        /**
         * URLs of processes in a system
         */
        private String[] urls;

        private Token token = null;
        private int capacity = 0;
        private long velocity = 1;
        private boolean inCriticalSection = false;

        /**
         * Estado del proceso
         */
        private ProcessState processState;

        /**
         * Default constructor following RMI conventions
         *
         * @param urls  URLs of participating processes
         * @param index index of current process
         * @throws RemoteException if RMI mechanisms fail
         */
        public Suzuki_kasami(String[] urls, int index, int capacity, int velocity) throws RemoteException {
                super();

                this.index = index;
                this.urls = urls;
                this.numProcesses = urls.length;
                this.capacity = capacity;
                this.velocity = (long) ((1. / velocity) * 1000);
                System.out.println("VELOCIDAD: " + this.velocity);
                reset();
                printStatus();
        }

        private void printStatus() {
                System.out.println("Proceso " + index + " en estado " + processState.toString());
        }

        public void reset() {

                this.RN = new ArrayList<Integer>(numProcesses);
                for (int i = 0; i < numProcesses; i++) {
                        RN.add(0);
                }
                processState = new ProcessState();

                token = null;
                inCriticalSection = false;
        }

        public void extract() throws RemoteException {
                // de seguro tenemos token
                int saca = Math.min(token.getCharactersRemaining(), capacity);
                for (int i = 0; i < saca; i++) {
                        System.out.print(token.readCharacter());
                        try {
                                Thread.sleep(velocity);
                        } catch (InterruptedException e) {
                                e.printStackTrace();
                        }
                }
                if(saca > 0){
                        System.out.println();
                }
        }

        public void request(int id, int seq) throws RemoteException {
                // actualizamos numero de seq del proceso id
                // el maximo entre el RN[id] y seq
                RN.set(id, Math.max(RN.get(id), seq));

                // si no estoy en seccion critica, tengo el token y la petición no es mia
                if (!inCriticalSection && token != null) {
                        if (index != id && (RN.get(id) > token.getLni(id))) {
                                String url = "rmi://localhost/process" + id;
                                try {
                                        System.out.println("regalando token");
                                        Suzuki_kasami_rmi dest = (Suzuki_kasami_rmi) Naming.lookup(url);
                                        dest.takeToken(token);
                                        token = null;
                                } catch (MalformedURLException | NotBoundException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                }
                        }
                }
        }

        public void waitToken() throws RemoteException {
                processState.status = Status.WAITINGTOKEN;
                printStatus();
                while (token == null) {
                        try {
                                Thread.sleep(TOKEN_WAIT_DELAY);
                        } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                        }
                }
        }

        public void takeToken(Token token) throws RemoteException {
                System.out.println("token tomado proceso " + index);
                inCriticalSection = true;
                this.token = token;
                processState.status = Status.CRITICALSECTION;
                printStatus();
        }

        public void kill() throws RemoteException {
                try {
                        Naming.unbind(urls[index]);
                } catch (MalformedURLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                } catch (NotBoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
                System.exit(0);
        }

        public void initializeExtractProcess() throws RemoteException {
                // broadcast request
                RN.set(index, RN.get(index) + 1);
                for (String url : urls) {
                        Suzuki_kasami_rmi dest;
                        try {
                                dest = (Suzuki_kasami_rmi) Naming.lookup(url);
                                dest.request(index, RN.get(index));
                        } catch (MalformedURLException e1) {
                                e1.printStackTrace();
                        } catch (NotBoundException e1) {
                                e1.printStackTrace();
                        } catch (RemoteException e) {
                                throw new RuntimeException(e);
                        }
                }
                waitToken();
                extract();
                try {
                        leaveToken();
                } catch (MalformedURLException | NotBoundException e) {
                        e.printStackTrace();
                }

        }

        private void leaveToken() throws RemoteException, MalformedURLException, NotBoundException {
                token.setLni(index, token.getLni(index) + 1);
                for (int i = 0; i < numProcesses; i++) {
                        if (!token.queueContains(i)) {
                                if (RN.get(i) == (token.getLni(i) + 1)) {
                                        token.addId(i);
                                }
                        }
                }
                // si la cola no esta vacia
                if (!token.queueIsEmpty()) {
                        int idProcess = token.popId();
                        String url = "rmi://localhost/process" + idProcess;
                        Suzuki_kasami_rmi dest = (Suzuki_kasami_rmi) Naming.lookup(url);
                        dest.takeToken(token);
                        token = null;
                        processState.status = Status.IDLE;
                        printStatus();
                } else {
                        // ?todos listos?
                        int leftCharacts = token.getCharactersRemaining();
                        System.out.println("recurso restante: " + leftCharacts);
                        if(leftCharacts>0){
                                //solicitar el token para una nueva ronda
                                this.initializeExtractProcess();
                        } else{
                                for (String url : urls) {
                                        if (!url.contains(String.valueOf(index))) {
                                                Suzuki_kasami_rmi dest = (Suzuki_kasami_rmi) Naming.lookup(url);
                                                dest.kill();
                                        }
                                }
                                kill();
                        }

                }
                inCriticalSection = false;

        }

        /**
         * Multithreads
         */
        public void run() {
                // System.out.println("comenzó proceso: " + index);
        }

}