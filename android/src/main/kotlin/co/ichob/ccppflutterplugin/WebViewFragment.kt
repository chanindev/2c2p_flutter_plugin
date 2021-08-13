package co.ichob.ccppflutterplugin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.ccpp.pgw.sdk.android.callback.APIResponseCallback
import com.ccpp.pgw.sdk.android.callback.PGWWebViewClientCallback
import com.ccpp.pgw.sdk.android.callback.PGWWebViewTransactionStatusCallback
import com.ccpp.pgw.sdk.android.core.PGWSDK
import com.ccpp.pgw.sdk.android.core.authenticate.PGWWebViewClient
import com.ccpp.pgw.sdk.android.enums.APIResponseCode
import com.ccpp.pgw.sdk.android.model.api.TransactionStatusRequest
import com.ccpp.pgw.sdk.android.model.api.TransactionStatusResponse

class WebViewFragment : Fragment() {
    private var mRedirectUrl: String = "https://www.chomchob.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mRedirectUrl = arguments?.getString(ARG_REDIRECT_URL) ?: ""
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val webView = WebView(requireContext())
        webView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = PGWWebViewClient()
        // webView.addJavascriptInterface(PGWJavaScriptInterface(mAPIResponseCallback),
        //         PGWJavaScriptInterface.JAVASCRIPT_TRANSACTION_RESULT_KEY)
        webView.setWebViewClient(
            PGWWebViewClient(
                mTransactionStatusCallback,
                mWebViewClientCallback
            )
        )


        webView.loadUrl(mRedirectUrl)
        return webView
    }

    private val mTransactionStatusCallback = PGWWebViewTransactionStatusCallback {
        //Do Transaction Status Inquiry API and close this WebView.
        //Step 1 : Generate payment token
        val paymentToken = it

        //Step 2: Construct transaction status inquiry request.
        val transactionStatusRequest = TransactionStatusRequest(paymentToken)
        transactionStatusRequest.setAdditionalInfo(true)

        //Step 3: Retrieve transaction status inquiry response.

        PGWSDK.getInstance().transactionStatus(
            transactionStatusRequest,
            object : APIResponseCallback<TransactionStatusResponse> {
                override fun onResponse(response: TransactionStatusResponse) {
                    if (response.responseCode == APIResponseCode.TransactionNotFound || response.responseCode == APIResponseCode.TransactionCompleted) {
                        //Read transaction status inquiry response.
                        val invoiceNo = response.invoiceNo
                        val result = Intent()
                        result.putExtra("invoiceNo", invoiceNo)
                        activity?.setResult(Activity.RESULT_OK, result)
                        activity?.finish()
                    } else {
                        //Get error response and display error
                        val result = Intent()
                        result.putExtra("errorMessage", response.responseDescription)
                        activity?.setResult(Activity.RESULT_OK, result)
                        activity?.finish()
                    }
                }

                override fun onFailure(error: Throwable) {
                    //Get error response and display error.
                    //Get error response and display error
                    val result = Intent()
                    result.putExtra("errorMessage", error.message)
                    activity?.setResult(Activity.RESULT_OK, result)
                    activity?.finish()
                }
            })
    }

    private val mWebViewClientCallback: PGWWebViewClientCallback =
        object : PGWWebViewClientCallback {
            override fun shouldOverrideUrlLoading(url: String) {
                //Log.i(TAG, "PGWWebViewClientCallback shouldOverrideUrlLoading : $url")
            }

            override fun onPageStarted(url: String) {
                // Log.i(TAG, "PGWWebViewClientCallback onPageStarted : $url")
            }

            override fun onPageFinished(url: String) {
                // Log.i(TAG, "PGWWebViewClientCallback onPageFinished : $url")
            }
        }

//    private val mAPIResponseCallback: APIResponseCallback<TransactionResultResponse> = object : APIResponseCallback<TransactionResultResponse> {
//        override fun onResponse(response: TransactionResultResponse) {
//            if (response.responseCode == APIResponseCode.TransactionCompleted) {
//                val invoiceNo = response.invoiceNo
//                val result = Intent()
//                result.putExtra("invoiceNo", invoiceNo)
//                activity?.setResult(Activity.RESULT_OK, result)
//                activity?.finish()
//            } else {
//                //Get error response and display error
//                val result = Intent()
//                result.putExtra("errorMessage", response.responseDescription)
//                activity?.setResult(Activity.RESULT_OK, result)
//                activity?.finish()
//            }
//        }
//
//        override fun onFailure(error: Throwable) {
//            //Get error response and display error
//            val result = Intent()
//            result.putExtra("errorMessage", error.message)
//            activity?.setResult(Activity.RESULT_OK, result)
//            activity?.finish()
//        }
//    }

    companion object {
        private const val ARG_REDIRECT_URL = "ARG_REDIRECT_URL"

        fun newInstance(redirectUrl: String): WebViewFragment {
            val fragment = WebViewFragment()
            val args = Bundle()
            args.putString(ARG_REDIRECT_URL, redirectUrl)
            fragment.arguments = args
            return fragment
        }
    }
}