/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.credentialmanager.sample

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.credentials.exceptions.publickeycredential.GetPublicKeyCredentialDomException
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.credentialmanager.sample.databinding.FragmentSignInBinding
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SignInFragment : Fragment() {

    private lateinit var credentialManager: CredentialManager
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private lateinit var listener: SignInFragmentCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as SignInFragmentCallback
        } catch (castException: ClassCastException) {
            /** The activity does not implement the listener.  */
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        credentialManager = CredentialManager.create(requireActivity())

        binding.signInWithSavedCredentials.setOnClickListener(signInWithSavedCredentials())
    }

    private fun signInWithSavedCredentials(): View.OnClickListener {
        return View.OnClickListener {

            lifecycleScope.launch {
                configureViews(View.VISIBLE, false)

                //Call getSavedCredentials() method to signin using passkey/password
                val data = getSavedCredentials()
                configureViews(View.INVISIBLE, true)
                //complete the authentication process after validating the public key credential to your server and let the user in.

                data?.let {
                    sendSignInResponseToServer()
                    listener.showHome(data)
                }

            }
        }
    }

    private fun configureViews(visibility: Int, flag: Boolean) {
        configureProgress(visibility)
        binding.signInWithSavedCredentials.isEnabled = flag
    }

    private fun configureProgress(visibility: Int) {
        binding.textProgress.visibility = visibility
        binding.circularProgressIndicator.visibility = visibility
    }

    private fun fetchAuthJsonFromServer(): String {
        //fetch authentication mock json
        return requireContext().readFromAsset("AuthFromServer")

    }

    private fun sendSignInResponseToServer(): Boolean {
        return true
    }

    private suspend fun getSavedCredentials(): String? {

        //create a GetPublicKeyCredentialOption() with necessary authentication json from server
        val getPublicKeyCredentialOption =
            GetPublicKeyCredentialOption(fetchAuthJsonFromServer(), null)
        //create a PasswordOption to retrieve all the associated user's password
        val getPasswordOption = GetPasswordOption()

        //call getCredential() with required credential options
        val result = try {
            credentialManager.getCredential(
                requireActivity(),
                GetCredentialRequest(
                    listOf(
                        getPublicKeyCredentialOption,
                        getPasswordOption
                    )
                )
            )
        } catch (e: NoCredentialException) {

            configureViews(View.INVISIBLE, true)
            Log.e("Auth", "getCredential failed with exception: " + e.message.toString())
            activity?.showErrorAlert(
                "Login failed: No credential available"
            )
            return null
        } catch(e: GetPublicKeyCredentialDomException)
        {
            configureViews(View.INVISIBLE, true)
            Log.e("Auth", "getCredential failed with exception: " + e.message.toString())
            activity?.showErrorAlert(
                "Login failed: Process cancelled by user"
            )
            return null
        } catch(e: GetCredentialCancellationException)
        {
            configureViews(View.INVISIBLE, true)
            Log.e("Auth", "getCredential failed with exception: " + e.message.toString())
            activity?.showErrorAlert(
                "Login failed: User canceled the selector"
            )
            return null
        }

        if (result.credential is PublicKeyCredential) {
            val cred = result.credential as PublicKeyCredential
            Log.i("Login JSON", cred.authenticationResponseJson)
            DataProvider.setSignedInThroughPasskeys(true)
            val json = Json { ignoreUnknownKeys = true }
            val jsonObject = json.parseToJsonElement(cred.authenticationResponseJson).jsonObject
            val response = jsonObject["response"]?.jsonObject
            val id = response?.get("userHandle")?.jsonPrimitive?.content!!
            return id
        }
        else if (result.credential is PasswordCredential) {
            val cred = result.credential as PasswordCredential
            DataProvider.setSignedInThroughPasskeys(false)
            return cred.id + cred.password
        }
        else if (result.credential is CustomCredential) {
            //If you are also using any external sign-in libraries, parse them here with the utility functions provided.

        }
        return null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        configureProgress(View.INVISIBLE)
        _binding = null
    }

    interface SignInFragmentCallback {
        fun showHome(data: String)
    }
}
