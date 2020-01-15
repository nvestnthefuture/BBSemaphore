/*********************************************
**Author: Jessica Byrd
**Date:   11/13/2019
**Purpose: Bounded Buffer Semaphore
**         My Semaphore class
**		   Objective: To use Semaphores
**Version: 1.0
*/

import java.util.Random;
import java.util.concurrent.Semaphore;

/********************Buffer*******************/
class Buffer
{

    private int[] buf;
    private int in = 0;
    private int out= 0;
    private int count = 0;
    private int size;

    Buffer(int size)
    {
        this.size = size;
        buf = new int[size];
    }

    public void put(int o)
    {
        buf[in] = o;
        ++count;
        in=(in+1) % size;
    }

    public int get()
    {
        int ret_val = buf[out];
        --count;
        out=(out+1) % size;
        return (ret_val);
    }

}

//End class Buffer

/*******************PRODUCER************************/
class Producer extends Thread
{

    Random bubba = new Random();
    public boolean stop = false;
    String name;

    Buffer buf_;
    Semaphore mutex, empty, full;
    int value;

    Producer(Buffer b, String n, Semaphore m, Semaphore e, Semaphore f, int val)
    {
        buf_ = b;
        name = n;
        mutex = m;
        empty = e;
        full = f;
        value = val;
    }

    public void run()
    {
      try
      {
        while(!stop)
        {
            value = bubba.nextInt(100);

            // produce, work
            Thread.sleep(750 + bubba.nextInt(1000));
            System.out.println(name + ", Put: " + value);

			//Wait
            empty.acquire();
            mutex.acquire();

            buf_.put(value);

            //Signal Release
            mutex.release();
            full.release();

        }
      }

      catch (InterruptedException e)
      {
        System.out.println("Producer: But I wasn't done!!!");
      }
    }
}
//End class Producer

/************************Middleman Class**********************/

class Middleman extends Thread
{
    Buffer buf_;
    Buffer buffer2;
    String name;
    public boolean stop = false;
    Semaphore mutex, empty, full;
    Semaphore mutex2, empty2, full2;


    Middleman(Buffer b, Buffer b2, String n, Semaphore m, Semaphore e, Semaphore f, Semaphore m2, Semaphore e2, Semaphore f2)
    {
        buf_ = b;
        buffer2 = b2;
        name = n;
        mutex = m;
        empty = e;
        full = f;

        mutex2 = m2;
		empty2 = e2;
        full2 = f2;


    }

    public void run()
    {
        try
        {
          int value;

          // consume, work
          Thread.sleep(1000);
          System.out.println("going: " + name);
          while(!stop)
          {

			//Acquire & Release
			//Geet Value
            full.acquire();
            mutex.acquire();
            value = buf_.get();
			mutex.release();
            empty.release();
            empty2.acquire();
            mutex2.acquire();
			buffer2.put(value);
			mutex2.release();
            full2.release();

            System.out.println(name + ", -------> value: " + value);

            // consume, work
            Thread.sleep(1500);
          }
        }

        catch (InterruptedException e)
        {
          System.out.println("Consumer: But I wasn't done!!!");
        }
      }
}



//End Middleman Class


/********************CONSUMER*******************************/

class Consumer extends Thread
{
    Buffer buf_;
    String name;
    public boolean stop = false;
    Semaphore mutex, empty, full;

    Consumer(Buffer b, String n, Semaphore m, Semaphore e, Semaphore f)
    {
        buf_ = b;
        name = n;
        mutex = m;
        empty = e;
        full = f;

    }

    public void run()
    {
        try
        {
          int value;

          // consume, work
          Thread.sleep(1000);
          System.out.println("going: " + name);
          while(!stop)
          {

			//Acquire & Release
            full.acquire();
            mutex.acquire();
            value = buf_.get();
            mutex.release();
            empty.release();

            System.out.println(name + ", -------> value: " + value);

            // consume, work
            Thread.sleep(1500);
          }
        }

        catch (InterruptedException e)
        {
          System.out.println("Consumer: But I wasn't done!!!");
        }
      }
}
//End class Consumer

/***************************Driver Class********************/

public class BBSemaphore
{

   public static void main (String args[])
   {
      int items = 20;
      int items2 = 20;

      //First buffer
      Buffer Buff1 = new Buffer(items);

      //Second buffer
      Buffer Buff2 = new Buffer(items2);

	  //Three Semaphores for the first buffer
      Semaphore mutex = new Semaphore(1);
      Semaphore full = new Semaphore(0);
      Semaphore empty = new Semaphore(items);

      //Three Semaphores for the second buffer
      Semaphore mutex2 = new Semaphore(1);
	  Semaphore full2 = new Semaphore(0);
	  Semaphore empty2 = new Semaphore(items2);

	  //Working Threads
      Producer Prod = new Producer(Buff1,"Producer 1", mutex, empty, full, 0);
      Middleman Mid = new Middleman(Buff1, Buff2, "Middleman 1", mutex, empty, full, mutex2, empty2, full2);
      Consumer Cons = new Consumer(Buff2,"Consumer 1", mutex, empty, full);

      Prod.start();
      Mid.start();
      Cons.start();

   }
}
//End of Program