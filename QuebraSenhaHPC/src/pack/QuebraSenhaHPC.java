package pack;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class QuebraSenhaHPC {

    public static void main(String[] args) {
        JFileChooser janela = new JFileChooser();

        //monitora a ação do usuário na árvore de diretório do sistema
        int operacao = janela.showOpenDialog(null);
        if (operacao == JFileChooser.APPROVE_OPTION) {

            //ref. do arquivo selecionado pelo user...
            File arquivo = janela.getSelectedFile();
            if (!arquivo.getAbsolutePath().contains(".zip")) {
                JOptionPane.showMessageDialog(null, "O arquivo selecionado deve ter ext. do tipo .zip", "Arquivo incorreto", JOptionPane.WARNING_MESSAGE);
                System.exit(0);
            }
            //------------------Agora é com vocês --------------------
            /*a partir daqui é onde entra a sua estratégia de solução
            Não modifique o código acima, apenas a partir daqui ;)
             */

            //"Chaveiro(s)" começa a trabalhar aqui ;)
            ChaveiroArquivo trab = new ChaveiroArquivo(arquivo);

            //Obtém o número de núcleos do processador
            int nThreads = Runtime.getRuntime().availableProcessors();

            //Cria uma pool de threads com o número de núcleos
            ExecutorService executor = Executors.newFixedThreadPool(nThreads);

            // Distribui a tabela ASCII entre as threads igualmente
            for (int i = 0; i < nThreads; i++) {
                int start = i * ((126 - 33) / nThreads) + 33;
                int end = (i + 1) * ((126 - 33) / nThreads) + 33 - 1;
                executor.submit(new TestaSenhaRunnable(arquivo, start, end));
            }

            // Encerra a pool de threads
            executor.shutdown();
            try {
                // Aguarda até que todas as threads terminem
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else {
            JOptionPane.showMessageDialog(null, "O arquivo não foi selecionado", "Arquivo???", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static class TestaSenhaRunnable implements Runnable {

        private final File arquivo;
        private final int start;
        private final int end;

        public TestaSenhaRunnable(File arquivo, int start, int end) {
            this.arquivo = arquivo;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            ChaveiroArquivo chaveiro = new ChaveiroArquivo(arquivo);
            boolean encontrouSenha = false; //Flag para se acharem a senha, a execução parar.
            for (int i = start; i <= end; i++) { //For para o teste das senhas
                for (int j = 33; j <= 126; j++) {
                    char[] senha = new char[5];
                    senha[0] = (char) i;
                    senha[1] = (char) j;
                    for (int k = 33; k <= 126; k++) {
                        senha[2] = (char) k;
                        for (int l = 33; l <= 126; l++) {
                            senha[3] = (char) l;
                            for (int m = 33; m <= 126; m++) {
                                senha[4] = (char) m;
                                if (chaveiro.tentaSenha(new String(senha))) {
                                    System.out.println("Senha correta: " + new String(senha));
                                    encontrouSenha = true; //A flag atualiza para true quando a senha é encontrada
                                    break;

                                }
                                if (encontrouSenha) { //Aqui a execução do programa é encerrada.
                                    System.exit(0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
