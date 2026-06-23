#!/usr/bin/env bash
set -euo pipefail

required_vars=(
  SIGNING_STORE_PASSWORD
  SIGNING_KEY_ALIAS
  SIGNING_KEY_PASSWORD
)

for name in "${required_vars[@]}"; do
  if [[ -z "${!name:-}" ]]; then
    echo "Missing required secret: ${name}" >&2
    exit 1
  fi
done

if [[ -n "${SIGNING_KEYSTORE:-}" ]]; then
  echo "$SIGNING_KEYSTORE" | base64 --decode > release.keystore
elif [[ ! -f release.keystore ]]; then
  echo "release.keystore not found and SIGNING_KEYSTORE secret is not set" >&2
  exit 1
fi

cat > signing.properties <<EOF
keystore.password=${SIGNING_STORE_PASSWORD}
key.alias=${SIGNING_KEY_ALIAS}
key.password=${SIGNING_KEY_PASSWORD}
EOF

echo "Signing configured (keystore: release.keystore, alias: ${SIGNING_KEY_ALIAS})"
