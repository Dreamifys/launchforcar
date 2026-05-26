const appShell = document.getElementById("appShell");
const modeToggle = document.getElementById("modeToggle");
const aiToggle = document.getElementById("aiToggle");
const aiClose = document.getElementById("aiClose");
const aiPanel = document.getElementById("aiPanel");

function updateModeButton() {
  const parkMode = appShell.classList.contains("park-mode");
  modeToggle.textContent = parkMode ? "切换到驾驶态" : "切换到停车态";
}

modeToggle.addEventListener("click", () => {
  appShell.classList.toggle("park-mode");
  appShell.classList.toggle("drive-mode");
  updateModeButton();
});

aiToggle.addEventListener("click", () => {
  aiPanel.classList.add("open");
});

aiClose.addEventListener("click", () => {
  aiPanel.classList.remove("open");
});

updateModeButton();