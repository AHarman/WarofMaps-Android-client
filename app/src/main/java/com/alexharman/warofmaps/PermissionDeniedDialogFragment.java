package com.alexharman.warofmaps;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * A fragment that simply pops up and tells the user that, no, we NEED access to your
 * location and then requests said permission.
 */
public class PermissionDeniedDialogFragment extends DialogFragment{

	PermissionDeniedDialogListener listener;

	/**
	 * Interface used to pass button press back to parent activity.
	 */
	public interface PermissionDeniedDialogListener {
		void onPermissionDialogButton();
	}


	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		// Ensure that parent class implements out interface
		try {
			listener = (PermissionDeniedDialogListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(getActivity().toString() + " must implement PermissionDeniedDialogListener to receive info");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.permission_denied_dialog_title)
				.setMessage(R.string.permission_denied_dialog_message)
				.setPositiveButton(R.string.permission_denied_dialog_affirm, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						listener.onPermissionDialogButton();
					}
				});
		return builder.create();
	}

}