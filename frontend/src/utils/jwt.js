// Decodes a JWT payload client-side, without verifying its signature.
// Signature verification is the server's job — this is only used to read
// claims (sub, role, exp) for UI purposes like showing the logged-in user
// or pre-empting an obviously expired token before it hits the API.
export function decodeJwt(token) {
  try {
    const payload = token.split('.')[1];
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export function isTokenExpired(token) {
  const claims = decodeJwt(token);
  if (!claims?.exp) return true;
  return claims.exp * 1000 < Date.now();
}
