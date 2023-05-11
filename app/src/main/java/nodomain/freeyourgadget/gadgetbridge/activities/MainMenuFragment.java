package nodomain.freeyourgadget.gadgetbridge.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import de.cketti.library.changelog.ChangeLog;
import nodomain.freeyourgadget.gadgetbridge.BuildConfig;
import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.util.AndroidUtils;
import nodomain.freeyourgadget.gadgetbridge.util.GB;

public class MainMenuFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {
    public static final int MENU_REFRESH_CODE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.fragment_main_menu, container, false);

        NavigationView navigationView = currentView.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /* This sucks but for the play store we're not allowed a donation link. Instead for
           the Bangle.js Play Store app we put a message in the About dialog via @string/about_description */
        if (BuildConfig.FLAVOR == "banglejs") {
            MenuItemImpl v = (MenuItemImpl) navigationView.getMenu().findItem(R.id.donation_link);
            if (v != null) v.setVisible(false);
        }

        return currentView;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivityForResult(settingsIntent, MENU_REFRESH_CODE);
                return false; //we do not want the drawer menu item to get selected
            case R.id.action_debug:
                Intent debugIntent = new Intent(getActivity(), DebugActivity.class);
                startActivity(debugIntent);
                return false;
            case R.id.action_data_management:
                Intent dbIntent = new Intent(getActivity(), DataManagementActivity.class);
                startActivity(dbIntent);
                return false;
            case R.id.action_notification_management:
                Intent blIntent = new Intent(getActivity(), NotificationManagementActivity.class);
                startActivity(blIntent);
                return false;
            case R.id.device_action_discover:
                launchDiscoveryActivity();
                return false;
            case R.id.action_quit:
                GBApplication.quit();
                return false;
            case R.id.donation_link:
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://liberapay.com/Gadgetbridge")); //TODO: centralize if ever used somewhere else
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return false;
            case R.id.external_changelog:
                ChangeLog cl = createChangeLog();
                try {
                    cl.getLogDialog().show();
                } catch (Exception ignored) {
                    GB.toast(getActivity().getBaseContext(), "Error showing Changelog", Toast.LENGTH_LONG, GB.ERROR);
                }
                return false;
            case R.id.about:
                Intent aboutIntent = new Intent(getActivity(), AboutActivity.class);
                startActivity(aboutIntent);
                return false;
        }

        return false;
    }

    private ChangeLog createChangeLog() {
        String css = ChangeLog.DEFAULT_CSS;
        css += "body { "
                + "color: " + AndroidUtils.getTextColorHex(getActivity().getBaseContext()) + "; "
                + "background-color: " + AndroidUtils.getBackgroundColorHex(getActivity().getBaseContext()) + ";" +
                "}";
        return new ChangeLog(getContext(), css);
    }

    private void launchDiscoveryActivity() {
        startActivity(new Intent(getActivity(), DiscoveryActivity.class));
    }
}