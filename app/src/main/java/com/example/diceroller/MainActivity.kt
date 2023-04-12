package com.example.diceroller

import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.math.abs

const val MAX_DICE = 3

class MainActivity : AppCompatActivity(),
    RollLengthDialogFragment.OnRollLengthSelectedListener{

    private var timerLength = 2000L
    private lateinit var optionsMenu: Menu
    private var numVisibleDice = MAX_DICE
    private var timer: CountDownTimer? = null
    private lateinit var diceList: MutableList<Dice>
    private lateinit var diceImageViewList: MutableList<ImageView>
    private var initTouchX = 0
    private var initTouchY = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Create list of Dice
        diceList = mutableListOf()
        for (i in 0 until MAX_DICE) {
            diceList.add(Dice(i + 1))
        }

        // Create list of ImageViews
        diceImageViewList = mutableListOf(
            findViewById(R.id.dice1), findViewById(R.id.dice2), findViewById(R.id.dice3))
        registerForContextMenu(diceImageViewList[0])
        showDice()


         // Moving finger left or right changes dice number
      diceImageViewList[0].setOnTouchListener { v, event ->
         var returnVal = true
         when (event.action) {
            MotionEvent.ACTION_DOWN -> {
               initTouchX = event.x.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
               val x = event.x.toInt()
               val y = event.y.toInt()

               // See if movement is at least 20 pixels
               if (abs(x - initTouchX) >= 20) {
                  if (x > initTouchX) {
                     diceList[0].number++
                  } else {
                     diceList[0].number--
                  }
                  showDice()
                  initTouchX = x
               }
                if (abs(y - initTouchY) >= 20) {
                    if (y > initTouchY) {
                        diceList[0].number++
                    } else {
                        diceList[0].number--
                    }
                    showDice()
                    initTouchY = y
                }
            }
            else -> returnVal = false
         }
         returnVal
      }
    }

    override fun onRollLengthClick(which: Int) {
        // Convert to milliseconds
        timerLength = 1000L * (which + 1)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.appbar_menu, menu)
        optionsMenu = menu!!
        return super.onCreateOptionsMenu(menu)
    }

    private fun showDice() {

        // Show visible dice
        for (i in 0 until numVisibleDice) {
            val diceDrawable = ContextCompat.getDrawable(this, diceList[i].imageId)
            diceImageViewList[i].setImageDrawable(diceDrawable)
            diceImageViewList[i].contentDescription = diceList[i].imageId.toString()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Determine which menu option was chosen
        return when (item.itemId) {
            R.id.action_one -> {
                changeDiceVisibility(1)
                showDice()
                true
            }
            R.id.action_two -> {
                changeDiceVisibility(2)
                showDice()
                true
            }
            R.id.action_three -> {
                changeDiceVisibility(3)
                showDice()
                true
            }
            R.id.action_stop -> {
                timer?.cancel()
                item.isVisible = false
                true
            }
            R.id.action_roll -> {
                rollDice()
                true
            }
            R.id.action_roll_length -> {
                val dialog = RollLengthDialogFragment()
                dialog.show(supportFragmentManager, "rollLengthDialog")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun changeDiceVisibility(numVisible: Int) {
        numVisibleDice = numVisible

        // Make dice visible
        for (i in 0 until numVisible) {
            diceImageViewList[i].visibility = View.VISIBLE
        }

        // Hide remaining dice
        for (i in numVisible until MAX_DICE) {
            diceImageViewList[i].visibility = View.GONE
        }
    }

    private fun rollDice() {
        optionsMenu.findItem(R.id.action_stop).isVisible = true
        timer?.cancel()

        // Start a timer that periodically changes each visible dice
        timer = object : CountDownTimer(timerLength, 100){
            override fun onTick(millisUntilFinished: Long) {
                for (i in 0 until numVisibleDice) {
                    diceList[i].roll()
                }
                showDice()
            }

            override fun onFinish() {
                optionsMenu.findItem(R.id.action_stop).isVisible = false
            }
        }.start()
    }

    override fun onCreateContextMenu(menu: ContextMenu?,
                                     v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_one -> {
                diceList[0].number++
                showDice()
                true
            }
            R.id.subtract_one -> {
                diceList[0].number--
                showDice()
                true
            }
            R.id.roll -> {
                rollDice()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

}