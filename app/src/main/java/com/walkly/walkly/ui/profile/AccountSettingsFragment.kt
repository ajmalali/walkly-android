package com.walkly.walkly.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.walkly.walkly.databinding.FragmentAccountSettingsBinding
import kotlinx.android.synthetic.main.fragment_account_settings.*

class AccountSettingsFragment : Fragment() {

    private lateinit var viewModel: AccountSettingsViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentAccountSettingsBinding.inflate(inflater, container, false)

        viewModel = ViewModelProviders.of(this).get(AccountSettingsViewModel::class.java)
        binding.viewModel = viewModel

        viewModel.userNameUpdateSuccess.observe(this, Observer {
            when(it) {
                "success" -> Toast.makeText(this.context, "User name has been updated", Toast.LENGTH_SHORT)
                    .show()
                "failure" -> Toast.makeText(this.context, "Failed to update user name", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        viewModel.userEmailUpdateSuccess.observe(this, Observer {
            when(it) {
                "success" -> Toast.makeText(this.context, "Email  has been updated", Toast.LENGTH_SHORT)
                    .show()
                "failure" -> Toast.makeText(this.context, "Failed to update email", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        viewModel.userPasswordUpdateSuccess.observe(this, Observer {
            when(it) {
                "success" -> Toast.makeText(this.context, "Password has been updated", Toast.LENGTH_SHORT)
                    .show()
                "failure" -> Toast.makeText(this.context, "Failed to update password", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        return binding.root
    }





}