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

        return binding.root
    }





}