#include "App.h"
#include "MainFrame.h"
#include <wx/wx.h>

wxIMPLEMENT_APP(App);

bool App::OnInit() {
	MainFrame* mainFrame = new MainFrame("My First GUI");
	mainFrame->SetClientSize(800, 850);
	mainFrame->Center();

	mainFrame->Show();

	return true;
};

