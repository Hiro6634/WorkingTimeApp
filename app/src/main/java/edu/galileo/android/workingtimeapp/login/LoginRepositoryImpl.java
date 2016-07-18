package edu.galileo.android.workingtimeapp.login;


import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import edu.galileo.android.workingtimeapp.domain.FirebaseHelper;
import edu.galileo.android.workingtimeapp.entities.User;
import edu.galileo.android.workingtimeapp.lib.EventBus;
import edu.galileo.android.workingtimeapp.lib.GreenRobotEventBus;
import edu.galileo.android.workingtimeapp.login.Events.LoginEvent;
import edu.galileo.android.workingtimeapp.login.ui.LoginActivity;

/**
 * Created by Hiro on 17/07/2016.
 */
public class LoginRepositoryImpl implements LoginRepository{
    private FirebaseHelper helper;
    private DatabaseReference dataReference;
    private DatabaseReference myUserReference;

    public LoginRepositoryImpl() {
        this.helper = FirebaseHelper.getInstance();
        this.dataReference = helper.getDataReference();
    }

    @Override
    public void signUp(String email, String password) {
        postEvent(LoginEvent.onSignUpSuccess);
    }

    @Override
    public void signIn(String email, String password) {
        try{
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword( email, password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            myUserReference = helper.getMyUserReference();
                            myUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User currentUser = dataSnapshot.getValue(User.class);
                                    if(currentUser == null){
                                        String email = helper.getAuthUserEmail();
                                        if(email != null){
                                            currentUser = new User();
                                            myUserReference.setValue(currentUser);
                                        }
                                    }
                                    postEvent(LoginEvent.onSignInSuccess);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            postEvent(LoginEvent.onSignInError, e.getMessage());
                        }
                    });
        }catch(Exception e){
            postEvent(LoginEvent.onSignInError, e.getMessage());
        }
    }

    @Override
    public void checkSession() {
        postEvent(LoginEvent.onFailedToRecoverSession);
    }

    private void postEvent(int type, String errorMessage ){
        LoginEvent event = new LoginEvent();

        event.setEventType(type);
        if(errorMessage != null){
            event.setErrorMessage(errorMessage);
        }

        EventBus eventBus = GreenRobotEventBus.getInstance();
        eventBus.post(event);
    }

    private void postEvent( int type){
        postEvent( type, null);
    }
}