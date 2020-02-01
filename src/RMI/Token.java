package RMI;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

public class Token implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        private Queue<Integer> queue;
        private int[] ln;
        private static int capacity;
        private static Boolean isInstantiated = false;
        private static BufferedReader reader;
        private static int charactersRemaining;
        private static  int initialCharacters;

        private Token(int numProcess) {
                queue = new LinkedList<Integer>();
                ln = new int[numProcess];
                for (int i = 0; i < numProcess; i++) {
                        ln[i] = 0;
                }
        }

        public static Token instantiate(int numProcesses, int inCapacity, String fileName) {
                if (!isInstantiated) {
                        isInstantiated = true;
                        capacity = inCapacity;
                        try {
                                reader = new BufferedReader(new FileReader(fileName));
                                // saber cantidad de recurso (letras) disponibles
                                charactersRemaining = countInitialResources();
                                reader.close();
                                reader = new BufferedReader(new FileReader(fileName));
                        } catch (FileNotFoundException e) {
                                e.printStackTrace();
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                        System.out.println("recurso inicial:" + charactersRemaining);
                        return new Token(numProcesses);
                }

                return null;
        }

        private static int countInitialResources() throws IOException {
                String data;
                while ((data = reader.readLine()) != null) {
                        charactersRemaining += data.length();
                }
                initialCharacters = charactersRemaining;
                return charactersRemaining;
        }

        /**
         * Obtener numero de secuencia LN[position] del proceso
         * 
         * @param index posicion del proceso
         * @return numero de secuencia registrado en LN
         */
        public int getLni(int index) {
                return ln[index];
        }

        /**
         * Registrar numero de secuencia en LN del proceso
         * 
         * @param index indice en LN
         * @param value valor a registrar
         */
        public void setLni(int index, int value) {
                ln[index] = value;
        }

        /**
         * Verificar si Cola contiene al proceso id
         * 
         * @param id id del proceso a verificar
         * @return Si el id del proceso está en la cola o no.
         */
        public Boolean queueContains(int id) {
                return queue.contains(id);
        }

        /**
         * Verificar si Cola esta vacía
         * 
         * @return
         */
        public Boolean queueIsEmpty() {
                return queue.isEmpty();
        }

        /**
         * Añadir id de proceso a la Cola
         * 
         * @param id id del proceso
         */
        public void addId(int id) {
                queue.add(id);
        }

        /**
         * Remover elemento en cabeza de la Cola
         * 
         * @return id del proceso removido
         */
        public int popId() {
                return queue.remove();
        }

        /**
         * Obtener la capacidad de extracción permitido
         * 
         * @return capacidad
         */
        public static int getCapacity() {
                return capacity;
        }

        /**
         * Obtener la cantidad de caracteres restantes
         * 
         * @return capacidad
         */
        public int getCharactersRemaining() {
                return charactersRemaining;
        }

        public String readCharacter() {
                char[] readed = new char[1];
                try {
                        reader.read(readed, 0, 1);
                } catch (IOException e) {
                        e.printStackTrace();
                }
                if(charactersRemaining > 0){
                        charactersRemaining -= 1;
                }
                return String.copyValueOf(readed);
        }
}