package com.yeraygarcia.recipes

import android.annotation.SuppressLint
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import android.widget.Toast
import com.yeraygarcia.recipes.database.entity.RecipeStep
import com.yeraygarcia.recipes.viewmodel.RecipeDetailViewModel
import kotlinx.android.synthetic.main.dialog_edit_step.view.*
import java.util.*

interface OnWebResponseListener {
    fun onSuccess()
    fun onError(errorCode: String, errorMessage: String)
}

class EditStepDialog : DialogFragment() {
    private var recipeStep: RecipeStep? = null

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val stepId = arguments?.getSerializable(argStepId) as UUID
        val dialogLayout = activity.layoutInflater.inflate(R.layout.dialog_edit_step, null)

        val viewModel = ViewModelProviders.of(activity).get(RecipeDetailViewModel::class.java)
        viewModel.getRecipeStep(stepId).observe(activity, Observer<RecipeStep> {
            recipeStep = it
            dialogLayout.textViewStepNumber.text = it?.sortOrder.toString()
            dialogLayout.editTextStepDescription.setText(it?.description)
        })

        // create dialog
        val alertDialog = AlertDialog.Builder(activity)
            .setView(dialogLayout)
            .setPositiveButton(R.string.save, null) // the listener is defined further down
            .setNegativeButton(R.string.cancel, null)
            .create()

        if (alertDialog.window != null) {
            // always show the keyboard
            alertDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        alertDialog.setOnShowListener { _ ->

            // focus the quantity field by default
            dialogLayout.editTextStepDescription.requestFocus()

            // define what happens when 'save' is clicked
            val positiveButton = (alertDialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {

                val newDescription = dialogLayout.editTextStepDescription.text.toString().trim()

                if (newDescription.isEmpty()) {
                    Toast.makeText(context, R.string.step_empty_error, Toast.LENGTH_LONG).show()
                } else {
                    recipeStep?.let { recipeStep ->
                        recipeStep.description = newDescription
                        viewModel.updateRecipeStep(recipeStep, object : OnWebResponseListener {

                            override fun onSuccess() {
                                alertDialog.dismiss()
                            }

                            override fun onError(errorCode: String, errorMessage: String) {
                                Toast.makeText(
                                    context,
                                    "${context?.getString(R.string.an_error_occurred)}\n$errorMessage",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        })
                    }
                }
            }
        }

        return alertDialog
    }

    companion object {

        const val argStepId = "argStepId"

        fun newInstance(stepId: UUID): EditStepDialog {
            return EditStepDialog().apply {
                arguments = Bundle().apply { putSerializable(argStepId, stepId) }
            }
        }
    }
}