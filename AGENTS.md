# AGENTS.md

## Cursor Cloud specific instructions

This repo is **Clash Meta for Android (CMFA)** — a single native Android app (Kotlin UI + an
embedded Go `mihomo` proxy kernel compiled to `libclash.so` via the NDK). There is **no
server/backend**; "running the product" means building the APK and running it on an Android
emulator/device. See `README.md` for the canonical build steps.

This is a **submodule project**: the `mihomo` kernel lives in the git submodule
`core/src/foss/golang/clash`, which is **required before building**. `.gitmodules` points it at the
fork `https://github.com/yygutn/mihomo` (tracking branch `Alpha`); the main repo pins a specific
kernel commit. `git submodule update --init --recursive` (run by the update script) checks out that
pinned commit — to advance the kernel, update the gitlink against the fork's `Alpha`, then rebuild.

### Environment (already provisioned in the VM snapshot)

The following are pre-installed and persisted in the VM image (do not reinstall):

- **JDK 21** (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`).
- **Android SDK** at `~/android-sdk` (`ANDROID_HOME`/`ANDROID_SDK_ROOT`): platform `android-35`,
  `build-tools;35.0.0`, NDK `29.0.14206865`, `emulator`, and the `android-35;default;x86_64`
  (AOSP) system image.
- **Custom Go 1.26** from `MetaCubeX/go` at `~/go-toolchain/go` (`GOROOT`), with the two
  `/.github/patch/*.patch` files already applied to `$GOROOT`. The system `go` (1.22) is wrong —
  the build needs this patched 1.26 toolchain, which is first on `PATH`.

These env vars are exported from `~/.bashrc`, so any **login shell** (`bash -l`) has them. The
update script only refreshes git submodules and `local.properties`; it intentionally does NOT
reinstall the SDK/Go or re-apply the Go patches (those live in the snapshot and re-applying patches
would fail).

### Build

`README.md` has the canonical build steps — follow those. The notes below are only the
cloud/dev deltas and corrections to `README.md`:

- **Use JDK 21, not "OpenJDK 11"** as the README says (CI and the actual build require 21).
- The README's "Golang" means the **patched MetaCubeX Go 1.26** described above, and the NDK must be
  `29.0.14206865` — not just any Go/NDK.
- For **development**, build the debug variant: `./gradlew --no-daemon app:assembleAlphaDebug`
  (README shows the release variant `app:assembleAlphaRelease`). Outputs land in
  `app/build/outputs/apk/alpha/<debug|release>/cmfa-*-<abi>-debug.apk` (one per ABI + `universal`).
- First build compiles the Go kernel for all 4 ABIs and takes several minutes; `app:downloadGeoFiles`
  (auto-wired into `assemble`) downloads geo databases from GitHub, so the build needs network access.
- Installed debug `applicationId` is `com.github.metacubex.clash.alpha`; launcher activity is
  `com.github.kr328.clash.MainActivity`.

### Lint / tests

- Lint: `./gradlew --no-daemon :app:lintAlphaDebug`.
- There are **no unit/instrumented tests** in this repo (CI only builds the APK).

### Running on an emulator (NO KVM available)

`/dev/kvm` is absent, so the emulator runs under full software CPU emulation (TCG) and is **very
slow**. Non-obvious gotchas learned here:

- Start it headless from a tmux session, e.g.:
  `emulator -avd <name> -no-audio -no-boot-anim -no-window -gpu swiftshader_indirect -no-snapshot -accel off -cores 4 -memory 4096`
- Use the **AOSP** system image (`...;default;x86_64`), NOT `google_apis` — the Google image spawns
  heavy background services (GMS, Wellbeing, Search) that constantly ANR on the slow CPU.
- Cold boot takes ~5–8 min; `-wipe-data` first boot longer. Wait for `getprop sys.boot_completed` = `1`.
- Do **not** `adb reboot` a headless swiftshader emulator — it breaks the render surface and
  `screencap` returns all-black (the app keeps running, only capture is black). Kill and cold-boot
  instead.
- Set `adb shell settings put global hide_error_dialogs 1` to reduce ANR dialogs (it isn't fully
  reliable under heavy load).
- **`adb shell input tap` uses real device pixels (1080x2400), not screenshot-scaled pixels.**
  Convert from a screenshot before tapping or your taps will miss. Transient "System UI isn't
  responding" dialogs appear under load; dismiss by tapping its "Wait" button (device coords) and
  the underlying app is unaffected.
- Inspect app state directly (debug build is debuggable):
  `adb shell run-as com.github.metacubex.clash.alpha sqlite3 databases/profiles 'SELECT name,type FROM pending'`.
  New profiles land in the `pending` table; they move to `imported` once a config is committed.
