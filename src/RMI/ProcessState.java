package RMI;

public class ProcessState {

        public enum Status {
                IDLE, WAITINGTOKEN, CRITICALSECTION
        }

        public Status status;

        public ProcessState() {
                this.status = Status.IDLE;
        }

        public String toString() {
                String message = "";

                switch (status) {
                case IDLE:
                        message = "Ocioso";
                        break;
                case WAITINGTOKEN:
                        message = "Esperando token";
                        break;
                case CRITICALSECTION:
                        message = "Sección crítica";
                        break;

                default:
                        break;
                }

                return message;
        }
}