package com.playgilround.schedule.client.signup.view;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.playgilround.schedule.client.R;
import com.playgilround.schedule.client.signup.SignUpAdapterContract;
import com.playgilround.schedule.client.signup.SignUpAdapterPresenter;
import com.playgilround.schedule.client.signup.model.User;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import org.joda.time.DateTime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.nispok.snackbar.Snackbar.SnackbarDuration;
import static com.nispok.snackbar.Snackbar.with;

/**
 * 19-02-23
 * SignUp Adapter
 */
public class SignUpAdapter extends RecyclerView.Adapter<SignUpAdapter.RootViewHolder> implements SignUpAdapterContract.View {

    private static final String TAG = SignUpAdapter.class.getSimpleName();

    private static final int SIGN_UP_MAX = 6;

    private static final int TYPE_NAME = 0;
    private static final int TYPE_EMAIL = 1;
    private static final int TYPE_PASSWORD = 2;
    private static final int TYPE_REPEAT_PASSWORD = 3;
    private static final int TYPE_NICK_NAME = 4;
    private static final int TYPE_BIRTH = 5;

    private SignUpAdapterContract.Presenter mPresenter;
    private Context mContext;

    private String password;
    private String strName;
    private String strEmail;
    private String strPw;
    private String strNickName;

    private int retPosition = 0;
    private OnButtonClick mCallback;

    public SignUpAdapter(Context context, OnButtonClick listener) {
        new SignUpAdapterPresenter(this);
        mContext = context;
        mCallback = listener;
    }

    @NonNull
    @Override
    public RootViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TYPE_NAME:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_signup_name, parent, false);
                return new NameViewHolder(view);
            case TYPE_EMAIL:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_signup_email, parent, false);
                return new EmailViewHolder(view);
            case TYPE_PASSWORD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_signup_password, parent, false);
                return new PasswordViewHolder(view);
            case TYPE_REPEAT_PASSWORD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_signup_password_check, parent, false);
                return new RepeatPasswordViewHolder(view);
            case TYPE_NICK_NAME:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_signup_nickname, parent, false);
                return new NickNameViewHolder(view);
            case TYPE_BIRTH:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_signup_birth, parent, false);
                return new BirthViewHolder(view);
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_signup_name, parent, false);
                return new EmptyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RootViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return SIGN_UP_MAX;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void setPresenter(SignUpAdapterContract.Presenter presenter) {
        mPresenter = presenter;
    }

    abstract class RootViewHolder extends RecyclerView.ViewHolder implements DatePickerDialog.OnDateSetListener {
        @BindView(R.id.ivBackBtn)
        ImageView ivBackBtn;

        @BindView(R.id.progress)
        ProgressBar mProgress;

        @BindView(R.id.tvSignUpTitle)
        TextView tvTitle;

        @BindView(R.id.tvSignUpContent)
        TextView tvContent;

        @BindView(R.id.etSignUpContent)
        EditText etContent;

        @BindView(R.id.ivNextBtn)
        ImageView ivNextBtn;

        String strContent = "";

        RootViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        abstract void bind(int position);

        @OnClick(R.id.ivBackBtn)
        void onBackButton() {
            retPosition = retPosition - 1;
            mCallback.onBackClick(retPosition);
        }

        @OnClick(R.id.ivNextBtn)
        void onNextButton() {
            retPosition = retPosition + 1;
            if (retPosition == 5) {
                tvTitle.setText(mContext.getString(R.string.text_signup_title_birth));
                mProgress.setProgress(6);
                tvContent.setVisibility(View.GONE);
                etContent.setVisibility(View.GONE);
                DateTime dateTime = new DateTime();
                int year = dateTime.getYear();
                int month = dateTime.getMonthOfYear() - 1;
                int day = dateTime.getDayOfMonth();
                showBirthDialog(year, month, day, R.style.birthDatePicker);
            } else {
                if (ivNextBtn.getDrawable().getConstantState() == ivNextBtn.getResources().getDrawable(R.mipmap.next_btn).getConstantState()) {
                    mCallback.onNextClick(retPosition);
                }
            }
        }

        void showBirthDialog(int year, int month, int day, int spinnerTheme) {
            new SpinnerDatePickerDialogBuilder()
                    .context(mContext)
                    .callback(this)
                    .spinnerTheme(spinnerTheme)
                    .defaultDate(year, month, day)
                    .build()
                    .show();
        }

        @Override
        public void onDateSet(com.tsongkha.spinnerdatepicker.DatePicker view, int year, int month, int day) {
            DateTime dateTime = new DateTime(year, month + 1, day, 0, 0);

            String strBirth = dateTime.toString(mContext.getString(R.string.text_date_year_month_day));
            Log.d(TAG, "Result ->" + strName + "//" + strEmail + "//" + strPw + "//" + strNickName + "//" + strBirth);

            User user = new User();

            user.setUserName(strName);
            user.setEmail(strEmail);
            user.setPassword(strPw);
            user.setNickName(strNickName);
            user.setBirth(strBirth);

            mCallback.onNextClick(SIGN_UP_MAX);
        }

        void showSnackbar(int position) {
            String snackContent = "";
            switch (position) {
                case 0:
                    snackContent = mContext.getString(R.string.text_signup_snackbar_name);
                    break;
                case 1:
                    snackContent = mContext.getString(R.string.text_signup_snackbar_email);
                    break;
                case 2:
                    snackContent = mContext.getString(R.string.text_signup_snackbar_password);
                    break;
                case 3:
                    snackContent = mContext.getString(R.string.text_signup_snackbar_password_check);
                    break;
                case 4:
                    snackContent = mContext.getString(R.string.text_signup_snackbar_nickname);
                    break;
            }

            SnackbarManager.show(
                    with(mContext)
                            .type(SnackbarType.MULTI_LINE)
                            .actionLabel("Close")
                            .actionColor(Color.parseColor("#FF8A80"))
                            .duration(SnackbarDuration.LENGTH_INDEFINITE)
                            .text(snackContent));
        }

        void dismissSnackbar() {
            SnackbarManager.dismiss();
        }

        /**
         * 이메일 형식 체크
         */
        boolean checkEmail(String email) {
            String mail = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
            Pattern p = Pattern.compile(mail);
            Matcher m = p.matcher(email);
            return m.matches();
        }

        /**
         * 패스워드 유효성검사
         * 영문, 숫자입력
         * 정규식 (영문, 숫자 8자리 이상)
         */
        boolean checkPassWord(String password) {
            String valiPass = "^(?=.*[a-z])(?=.*[0-9]).{8,}$";

            Pattern pattern = Pattern.compile(valiPass);
            Matcher matcher = pattern.matcher(password);

            return matcher.matches();
        }
    }

    class NameViewHolder extends RootViewHolder {

        NameViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        void bind(int position) {
            etContent.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    strContent = etContent.getText().toString().trim();
                    if (strContent.length() > 1) {
                        strName = strContent;
                        ivNextBtn.setImageResource(R.mipmap.next_btn);
                        dismissSnackbar();
                    } else {
                        //다음 버튼 비활성화 아이콘 디자이너한테 받아야 함.
                        ivNextBtn.setImageResource(R.mipmap.disable_btn);
                        showSnackbar(0);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
    }

    class EmailViewHolder extends RootViewHolder {

        EmailViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        void bind(int position) {
            etContent.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    strContent = etContent.getText().toString().trim();
                    if (checkEmail(strContent)) {
                        strEmail = strContent;
                        ivNextBtn.setImageResource(R.mipmap.next_btn);
                        dismissSnackbar();
                    } else {
                        //다음 버튼 비활성화 아이콘 디자이너한테 받아야 함.
                        ivNextBtn.setImageResource(R.mipmap.disable_btn);
                        showSnackbar(1);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

        }
    }

    class PasswordViewHolder extends RootViewHolder {

        PasswordViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        void bind(int position) {
            etContent.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    strContent = etContent.getText().toString().trim();
                    if (checkPassWord(strContent)) {
                        password = strContent;
                        ivNextBtn.setImageResource(R.mipmap.next_btn);
                        dismissSnackbar();
                    } else {
                        //다음 버튼 비활성화 아이콘 디자이너한테 받아야 함.
                        ivNextBtn.setImageResource(R.mipmap.disable_btn);
                        showSnackbar(2);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
    }

    class RepeatPasswordViewHolder extends RootViewHolder {

        RepeatPasswordViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        void bind(int position) {
            etContent.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    strContent = etContent.getText().toString().trim();
                    if (password.equals(strContent)) {
                        strPw = strContent;
                        ivNextBtn.setImageResource(R.mipmap.next_btn);
                        dismissSnackbar();
                    } else {
                        //다음 버튼 비활성화 아이콘 디자이너한테 받아야 함.
                        ivNextBtn.setImageResource(R.mipmap.disable_btn);
                        showSnackbar(3);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
    }

    class NickNameViewHolder extends RootViewHolder {

        NickNameViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        void bind(int position) {
            etContent.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    strContent = etContent.getText().toString().trim();
                    strNickName = strContent;
                    ivNextBtn.setImageResource(R.mipmap.next_btn);
                    dismissSnackbar();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

        }
    }

    class BirthViewHolder extends RootViewHolder {

        BirthViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        void bind(int position) {

        }
    }

    class EmptyViewHolder extends RootViewHolder {

        EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        void bind(int position) {

        }
    }

    public interface OnButtonClick {
        void onNextClick(int position);

        void onBackClick(int position);
    }
}