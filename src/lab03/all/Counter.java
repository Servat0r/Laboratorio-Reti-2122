package lab03.all;

import util.ThreadUtils;

/**
* Counter modella il contatore
* @author Samuel Fabrizi
* @version 1.0
*/
public class Counter {
    /**
     * rappresenta il contatore
     */
    private int contatore = 0;

    /**
     * incrementa di 1 unità  il valore del contatore
     */
    public void increment(){
        contatore++;
        ThreadUtils.Sleep(0, 100);
    }

    /**
     *
     * @link Counter#contatore
     */
    public int get(){
    	ThreadUtils.Sleep(0, 100);
        return contatore;
    }
}