# Universal Replicant Engine — Android starter

This is a starter native Android project, not the finished AI reconstruction product.

Included: native live-wallpaper service, rotation-vector sensor response while visible, and a diagnostic scene to verify the wallpaper pipeline. Android's system wallpaper picker controls whether it is applied to the lock screen, home screen, or both; device support varies.

Not included yet: arbitrary photo/video reconstruction, AI depth estimation, occlusion completion, true 3D scene generation, exact clock extraction, cloud GPU inference, or iOS support. Those require additional models, backend services, rights-cleared assets and device testing. A single photo/video cannot reveal unseen geometry exactly.

Build: the included GitHub Actions workflow builds a debug APK for testing. This is an early technical foundation, not a finished product.
