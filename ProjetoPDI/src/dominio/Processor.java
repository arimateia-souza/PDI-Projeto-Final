package dominio;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dominio.ImaJ.ImaJ;
import dominio.ImaJ.Properties;
import persistencia.ImageReader;
import visao.ImageShow;

public class Processor {

    public List<Entity> process(File file) {
        ImageShow imageShow = new ImageShow();

        ArrayList<Entity> list = new ArrayList<>();
        int[][][] im = ImageReader.imRead(file.getPath());

        int[][][] im_blur = ImaJ.imGaussian(im, 5);

        int[][] im_gray = ImaJ.rgb2gray(im_blur);

        int[][] im_red = ImaJ.splitChannel(im_blur, 0);
        imageShow.imShow(im_red, file.getPath());

        int[][] im_green = ImaJ.splitChannel(im_blur, 1);
        imageShow.imShow(im_green, file.getPath());

        int[][] im_blue = ImaJ.splitChannel(im_blur, 2);
        imageShow.imShow(im_blue, file.getPath());

        boolean[][] im_mask = ImaJ.im2bw(im_red);
        imageShow.imShow(im_mask, file.getPath());

        // boolean[][] im_mask = ImaJ.im2bw(im_red);
        im_mask = ImaJ.bwDilate(im_mask, 70);
        im_mask = ImaJ.bwErode(im_mask, 70);
        imageShow.imShow(im_mask, file.getPath());

        ArrayList<Properties> frutas = ImaJ.regionProps(im_mask);

        // Ordena as frutas por área (do menor para o maior)
        Collections.sort(frutas, Comparator.comparingDouble(o -> o.area));

        for (int i = 0; i < frutas.size(); i++) {
            int[][][] im2 = ImaJ.imCrop(im, frutas.get(i).boundingBox[0], frutas.get(i).boundingBox[1],
                    frutas.get(i).boundingBox[2], frutas.get(i).boundingBox[3]);

           
            int totalRed = 0;
            int totalGreen = 0;
            int totalBlue = 0;
            int totalPixels = 0;
            String classificacao = " ";

            // Calcula o raio da moeda aproximadamente com base na área
            double raio = Math.sqrt(frutas.get(i).area / Math.PI);

            // Aplicando máscara na imagem original
            for (int x = 0; x < im2.length; x++) {
                for (int y = 0; y < im2[0].length; y++) {
                    // Se é pixel de fundo
                    if (!frutas.get(i).image[x][y]) {
                        im2[x][y] = new int[] { 0, 0, 0 };
                    } else {
                        // Obj
                        int r = im2[x][y][0];
                        int g = im2[x][y][1];
                        int b = im2[x][y][2];
                        totalRed += r;
                        totalGreen += g;
                        totalBlue += b;
                        totalPixels++;
                    }
                }
            }

            int mediaRed = totalRed / totalPixels;
            int mediaGreen = totalGreen / totalPixels;
            int mediaBlue = totalBlue / totalPixels;
            //System.out.println("Antes da condição");
            
            System.out.println("mediaRed: " + mediaRed + ", mediaGreen: " + mediaGreen + ", totalBlue: " + mediaBlue);

            if (mediaRed > 115 && mediaGreen <= 49 || raio > 700.0000000000000) {
                classificacao = "manga";
                //System.out.println("É uma manga");
                
                
            } else if(mediaRed >= 120 && mediaRed <= 130 && mediaGreen >= 99 && mediaGreen <= 130 || raio > 500.0000000000000 && raio > 700.0000000000000 ) {
                classificacao = "tamarindo";
                
                
            }else if(mediaRed >= 140 && mediaGreen >= 120 && raio > 200.0000000000000) {
                classificacao = "caja";
                
                
            }else {
            	classificacao = "sem classificacao";
            }

            //System.out.println("Depois da condição");


            // Cria uma nova instância da classe Entity com a média das cores
            list.add(new Entity(frutas.get(i).area, raio,
                    String.format("RGB(%d, %d, %d)", mediaRed, mediaGreen, mediaBlue),
                    file.getPath().split("\\.")[0] + "_" + i + ".png", classificacao));

            System.out.println("Objeto " + i + ": Area = " + frutas.get(i).area + ", Raio = " + raio
                    + ", Média das Cores: " + mediaRed + ", " + mediaGreen + ", " + mediaBlue);
        }

        return list;
    }
}
