package com.shivam.puppyadoption.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.shivam.puppyadoption.R
import com.shivam.puppyadoption.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        // firebase
        auth = FirebaseAuth.getInstance()

        binding.loginGotoReg.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.loginForgotPass.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_loginFragment_to_resetPasswordFragment)
        }

        binding.loginBtn.setOnClickListener {
            val email: String = binding.loginEmail.text.toString()
            val pass: String = binding.loginPassword.text.toString()

            if (!isEmpty(email) && !isEmpty(pass)) {
                loginUser(email, pass)
            } else if (isEmpty(email) && isEmpty(pass)) {
                binding.loginEmailLayout.error = "Email can't be empty"
                binding.loginPasswordLayout.error = "Password can't be empty"
            } else if (isEmpty(email)) {
                binding.loginEmailLayout.error = "Email can't be empty"
            } else if (isEmpty(pass)) {
                binding.loginPasswordLayout.error = "Password can't be empty"
            }
        }

        return binding.root
    }

    private fun loginUser(email: String, pass: String) {
        toggleProgressBar()
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                view?.findNavController()?.navigate(R.id.action_loginFragment_to_mainActivity)
            } else {
                toggleProgressBar()
                Toast.makeText(context, "Error : ${task.exception?.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun toggleProgressBar() {
        if (binding.loginProgressBar.visibility == View.VISIBLE) {
            binding.loginProgressBar.visibility = View.INVISIBLE
            binding.loginBtn.isEnabled = true
        } else {
            binding.loginProgressBar.visibility = View.VISIBLE
            binding.loginBtn.isEnabled = false
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            view?.findNavController()?.navigate(R.id.action_loginFragment_to_mainActivity)
        }
    }

    private fun isEmpty(input: String): Boolean = input.trim().isEmpty()
}