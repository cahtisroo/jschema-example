function pageLoaded() {
	// Focus input field
	var input = document.getElementById('ticker-input');
	input.focus();
	input.select();

	// Disable autocomplete
	var form = document.getElementById('form');
	form.setAttribute('autocomplete','off');
}