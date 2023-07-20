package primenumber;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

public class Main {

    public static File selecionaDiretorioRaiz() {
        JFileChooser janelaSelecao = new JFileChooser(".");
        janelaSelecao.setControlButtonsAreShown(true);

        //conf. do filtro de selecao
        janelaSelecao.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File arquivo) {
                return arquivo.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Diretório";
            }
        });

        janelaSelecao.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        //avaliando a acao do usuario na selecao da pasta de inicio da busca
        int acao = janelaSelecao.showOpenDialog(null);

        if (acao == JFileChooser.APPROVE_OPTION) {
            return janelaSelecao.getSelectedFile();
        } else {
            return null;
        }
    }

    public static void main(String[] args) {
        //selecao de um diretorio para iniciar a busca
        File pastaInicial = selecionaDiretorioRaiz();

        if (pastaInicial == null) {
            JOptionPane.showMessageDialog(null, "Você deve selecionar uma pasta para o processamento",
                    "Selecione o arquivo", JOptionPane.WARNING_MESSAGE);
        } else {
            //...Modifique a partir daqui
            //AQUI você deve explorar a pasta, arquivos e subpastas...

            // Armazena os arquivos de texto encontrados em um ArrayList
            ArrayList<File> arquivosTexto = new ArrayList<>();
            exploreDirectory(pastaInicial, arquivosTexto);

            // Variáveis para armazenar o maior número primo encontrado
            int maiorPrimo = 0;
            String caminhoMaiorPrimo = "";

            // Cria threads e processadores de arquivo separadamente
            ArrayList<Thread> threads = new ArrayList<>();
            ArrayList<ProcessadorArquivo> processadores = new ArrayList<>();

            for (File arquivo : arquivosTexto) {
                ProcessadorArquivo processador = new ProcessadorArquivo(arquivo);
                Thread thread = new Thread(processador);
                thread.start();
                threads.add(thread);
                processadores.add(processador);
            }

            // Aguarda até que todas as threads terminem
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Encontra o maior número primo entre os resultados dos processadores
            for (ProcessadorArquivo processador : processadores) {
                int numeroPrimo = processador.getMaiorNumeroPrimo();

                if (numeroPrimo > maiorPrimo) {
                    maiorPrimo = numeroPrimo;
                    caminhoMaiorPrimo = processador.getCaminhoMaiorPrimo();
                }
            }

            // Calcula o tempo total de execução
            long tempoInicio = processadores.get(0).getTempoInicio();
            long tempoFim = System.currentTimeMillis();
            long tempoTotal = tempoFim - tempoInicio;

            // Formatar o tempo no formato "X minutos X segundos X milissegundos"
            long minutos = tempoTotal / (60 * 1000);
            long segundos = (tempoTotal / 1000) % 60;
            long milissegundos = tempoTotal % 1000;

            // Exibe o resultado
            String mensagem = "O maior número primo é: " + maiorPrimo
                    + "\nCaminho do arquivo: " + caminhoMaiorPrimo
                    + "\nTempo de execução: " + minutos + " minutos " + segundos + " segundos " + milissegundos + " milissegundos";
            JOptionPane.showMessageDialog(null, mensagem, "Resultado", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Método para explorar a pasta, arquivos e subpastas
    public static void exploreDirectory(File pasta, ArrayList<File> arquivosTexto) {

        if (pasta.isDirectory()) {
            File[] arquivos = pasta.listFiles();
            if (arquivos != null) {
                for (File arquivo : arquivos) {
                    if (arquivo.isDirectory()) {
                        exploreDirectory(arquivo, arquivosTexto);
                    } else if (arquivo.isFile() && arquivo.getName().toLowerCase().endsWith(".txt")) {
                        arquivosTexto.add(arquivo);
                    }
                }
            }
        }
    }

    // Classe para processar cada arquivo
    static class ProcessadorArquivo implements Runnable {

        private File arquivo;
        private int maiorNumeroPrimo;
        private String caminhoMaiorPrimo;
        private long tempoInicio;

        public ProcessadorArquivo(File arquivo) {
            this.arquivo = arquivo;
            this.maiorNumeroPrimo = 0;
            this.caminhoMaiorPrimo = "";
        }

        public int getMaiorNumeroPrimo() {
            return maiorNumeroPrimo;
        }

        public String getCaminhoMaiorPrimo() {
            return caminhoMaiorPrimo;
        }

        public long getTempoInicio() {
            return tempoInicio;
        }

        @Override
        public void run() {
            tempoInicio = System.currentTimeMillis();

            try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
                String linha;
                int maiorNumeroPrimo = 0;

                while ((linha = reader.readLine()) != null) {
                    String[] numeros = linha.split("\\s+|,");

                    for (String numero : numeros) {
                        if (numero.matches("-?\\d+")) {
                            int valor = Integer.parseInt(numero);
                            if (isNumeroPrimo(valor)) {
                                if (valor > maiorNumeroPrimo) {
                                    maiorNumeroPrimo = valor;
                                }
                            }
                        }
                    }
                }

                this.maiorNumeroPrimo = maiorNumeroPrimo;
                this.caminhoMaiorPrimo = arquivo.getAbsolutePath();
            } catch (IOException e) {
                System.err.println("Erro ao ler o arquivo: " + arquivo.getAbsolutePath());
                e.printStackTrace();
            }
        }

        private boolean isNumeroPrimo(int numero) {
            //Faz a verificação se é primo
            if (numero < 2) {
                return false;
            }

            for (int i = 2; i <= Math.sqrt(numero); i++) {
                if (numero % i == 0) {
                    return false;
                }
            }

            return true;
        }
    }
}