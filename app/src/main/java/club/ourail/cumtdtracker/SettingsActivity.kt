package club.ourail.cumtdtracker

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File


class SettingsActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        this.window.statusBarColor = ContextCompat.getColor(this, R.color.statusBarColor)
        when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }

        toolbar = findViewById(R.id.toolbar_settings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.list_container, SettingsFragment())
                .commit()
        }


    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            val pScreen: PreferenceScreen = preferenceManager.createPreferenceScreen(context)
            preferenceScreen = pScreen;
            addPreferencesFromResource(R.xml.preferences)

            val appVersion = Preference(context)
            appVersion.title = "App Version"
            appVersion.summary = resources.getString(R.string.versionName);
            pScreen.addPreference(appVersion)

            val changesetId = Preference(context)
            changesetId.title = "Stops Data Changeset ID"
            changesetId.summary =
                PreferenceManager.getDefaultSharedPreferences(context).getString("version", "")
            pScreen.addPreference(changesetId)

            val updateJson = Preference(context)
            updateJson.title = "Update Stops Data"
            updateJson.setOnPreferenceClickListener {
                val cid = PreferenceManager.getDefaultSharedPreferences(context).getString(
                    "version",
                    ""
                )

                val client = OkHttpClient.Builder().build()

                val retrofit =
                    Retrofit.Builder()
                        .baseUrl("https://developer.cumtd.com/")
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .client(client)
                        .build()

                val service = retrofit.create(getstops::class.java)
                val call = service.getStopString(
                    getString(R.string.api_key),
                    cid!!
                )

                call.enqueue(object : Callback<String> {
                    override fun onResponse(
                        call: Call<String>,
                        response: Response<String>
                    ) {
                        if (response.isSuccessful) {
                            val obj = JSONObject(response.body())
                            if (!obj.getBoolean("new_changeset"))
                                Toast.makeText(context, "No Updates", Toast.LENGTH_SHORT).show()
                            else {
                                val changesetID = obj.getString("changeset_id")
                                changesetId.summary = changesetID
                                PreferenceManager.getDefaultSharedPreferences(context).edit()
                                    .putString("version", changesetID).apply()

                                val file = File(context?.filesDir, "getstops.json")
                                file.writeText(response.body()!!)
                                Toast.makeText(context, "Update Successful", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                    }
                })
                true
            }
            pScreen.addPreference(updateJson)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}



