package club.ourail.cumtdtracker.MainActivity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import club.ourail.cumtdtracker.R


class SaferideFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_saferide, container, false)

        val button1 = view.findViewById<Button>(R.id.call_safewalk)
        button1.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.data = Uri.parse("tel:+1 (217) 333-1216")
            startActivity(callIntent)
        }

        val button2 = view.findViewById<Button>(R.id.call_saferide)
        button2.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.data = Uri.parse("tel:+1 217-265-RIDE")
            startActivity(callIntent)
        }

        val button3 = view.findViewById<Button>(R.id.open_saferide)
        button3.setOnClickListener {
            var launchIntent: Intent? =
                context?.packageManager?.getLaunchIntentForPackage("com.routematch.cumtd")
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launchIntent)
            } else {
                launchIntent = Intent(Intent.ACTION_VIEW)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                launchIntent.data = Uri.parse("market://details?id=" + "com.routematch.cumtd")
                startActivity(launchIntent)
            }
        }


        return view
    }
}