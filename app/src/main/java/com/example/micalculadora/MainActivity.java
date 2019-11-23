package com.example.micalculadora;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.material.snackbar.Snackbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.etInput)
    EditText etInput;
    @BindView(R.id.contentMain)
    RelativeLayout contentMain;

    private boolean isEditInProgress = false;
    private int minLength;
    private int textSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        minLength = getResources().getInteger(R.integer.main_min_length);
        textSize = getResources().getInteger(R.integer.main_input_textSize);
        configEditText();
    }

    private void configEditText() {
        /*etInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                input.hideSoftInputFromWindow(view.getWindowToken(),0);
            }
        });*/

        etInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    if(motionEvent.getRawX() >= (etInput.getRight() -
                            etInput.getCompoundDrawables()[Constantes.DRAWABLE_RIGHT].getBounds().width())){
                        if(etInput.length()>0){
                            final int length = etInput.getText().length();
                            etInput.getText().delete(length-1, length);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!isEditInProgress && Metodos.canReplaceOperator(charSequence)){
                    isEditInProgress = true;
                    etInput.getText().delete(etInput.getText().length()-2, etInput.getText().length()-1);
                }

                if(charSequence.length() > minLength){
                    etInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize -
                            (((charSequence.length() - minLength) * 2) + (charSequence.length() - minLength)));
                } else {
                    etInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                isEditInProgress = false;
            }
        });
    }

    @OnClick({R.id.btnZero, R.id.btnOne, R.id.btnTwo, R.id.btnThree, R.id.btnFour, R.id.btnFive,
            R.id.btnSix, R.id.btnSeven, R.id.btnEight, R.id.btnNine, R.id.btnPoint})
    public void onClickNumber(View view){
        final String valStr = ((Button)view).getText().toString();
        switch (view.getId()){
            case R.id.btnZero:
            case R.id.btnOne:
            case R.id.btnTwo:
            case R.id.btnThree:
            case R.id.btnFour:
            case R.id.btnFive:
            case R.id.btnSix:
            case R.id.btnSeven:
            case R.id.btnEight:
            case R.id.btnNine:
                etInput.getText().append(valStr);
                break;
            case R.id.btnPoint:
                final String operation = etInput.getText().toString();
                final String operator = Metodos.getOperator(operation);

                final int count = operation.length() - operation.replace(".", "").length();

                if(!operation.contains(Constantes.POINT) ||
                        (count<2 && (!operator.equals(Constantes.OPERATOR_NULL)))){
                    etInput.getText().append(valStr);
                }
                break;
        }
    }

    @OnClick({R.id.btnClear, R.id.btnDiv, R.id.btnMultiplication, R.id.btnSub,
            R.id.btnSum, R.id.btnResult})
    public void OnClickController(View view){
        switch (view.getId()){
            case R.id.btnClear:
                etInput.setText("");
                break;
            case R.id.btnDiv:
            case R.id.btnMultiplication:
            case R.id.btnSub:
            case R.id.btnSum:
                resolve(false);

                final String operator = ((Button)view).getText().toString();
                final String operation = etInput.getText().toString();

                final String endCharacter = operation.isEmpty()? "":
                        operation.substring(operation.length() - 1);

                if(operator.equals(Constantes.OPERATOR_SUB)){
                    if(operation.isEmpty()||
                            (!(endCharacter.equals(Constantes.OPERATOR_SUB)) &&
                        !(endCharacter.equals(Constantes.POINT)))){
                        etInput.getText().append(operator);
                    }
                }else{
                    if(!operation.isEmpty() &&
                            !(endCharacter.equals(Constantes.OPERATOR_SUB)) &&
                            !(endCharacter.equals(Constantes.POINT))) {
                        etInput.getText().append(operator);
                    }
                }
                break;
            case R.id.btnResult:
                resolve(true);
                break;
        }
    }

    private void resolve(boolean fromResult) {
        Metodos.tryResolve(fromResult, etInput, new OnResolveCallback() {
            @Override
            public void onShowMessage(int errorRes) {
                    showMessage(errorRes);
            }

            @Override
            public void onIsEditing() {
                isEditInProgress = true;
            }
        });
    }

    private void showMessage(int errorRes) {
        Snackbar.make(contentMain, errorRes, Snackbar.LENGTH_SHORT).show();
    }
}
