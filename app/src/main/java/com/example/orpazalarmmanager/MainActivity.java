package com.example.orpazalarmmanager;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private EditText eventTitleEditText;
    private EditText eventDescriptionEditText;
    private Button addEventButton;
    private Button selectDateButton;
    private Button selectTimeButton;

    private int year, month, day, hour, minute;
    private boolean dateSelected = false;
    private boolean timeSelected = false;

    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eventTitleEditText = findViewById(R.id.eventTitle);
        eventDescriptionEditText = findViewById(R.id.eventDescription);
        addEventButton = findViewById(R.id.addEventButton);
        selectDateButton = findViewById(R.id.selectDateButton);
        selectTimeButton = findViewById(R.id.selectTimeButton);

        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        selectTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        addEventButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_CALENDAR},
                        PERMISSION_REQUEST_CODE);
            } else {
                addEventToCalendar();
            }
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            this.year = year;
            this.month = month;
            this.day = dayOfMonth;
            dateSelected = true;
            selectDateButton.setText(String.format("%d-%d-%d", dayOfMonth, month + 1, year));
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            this.hour = hourOfDay;
            this.minute = minute;
            timeSelected = true;
            selectTimeButton.setText(String.format("%02d:%02d", hourOfDay, minute));
        }, hour, minute, true);
        timePickerDialog.show();
    }

    private void addEventToCalendar() {
        String title = eventTitleEditText.getText().toString();
        String description = eventDescriptionEditText.getText().toString();

        if (title.isEmpty()) {
            Toast.makeText(this, "אנא הכנס כותרת לאירוע", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dateSelected || !timeSelected) {
            Toast.makeText(this, "אנא בחר תאריך ושעה לאירוע", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar startTime = Calendar.getInstance();
        startTime.set(year, month, day, hour, minute);

        Calendar endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 1);

        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, 1); // שנה את ה-ID של הלוח שנה שלך במידת הצורך
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION, description);
        values.put(CalendarContract.Events.DTSTART, startTime.getTimeInMillis());
        values.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "UTC");

        try {
            cr.insert(CalendarContract.Events.CONTENT_URI, values);
            Toast.makeText(this, "אירוע נוסף ליומן", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "הוספת האירוע נכשלה", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addEventToCalendar();
            } else {
                Toast.makeText(this, "הרשאה נדחתה", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
