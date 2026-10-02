const DEFAULT_BASE_URL = "http://localhost:8080";

export class IncentivosApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = "IncentivosApiError";
    this.status = status;
  }
}

export function createIncentivosApi({
  baseUrl = process.env.INCENTIVOS_API_URL || DEFAULT_BASE_URL,
  fetchImpl = fetch,
} = {}) {
  const normalizedBaseUrl = baseUrl.replace(/\/+$/, "");

  return async function request(path, { method = "GET", body, allowNotFound = false } = {}) {
    const response = await fetchImpl(`${normalizedBaseUrl}${path}`, {
      method,
      headers: body === undefined ? { Accept: "application/json" } : {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
      body: body === undefined ? undefined : JSON.stringify(body),
      signal: AbortSignal.timeout(10_000),
    });

    const responseText = await response.text();
    if (allowNotFound && response.status === 404) {
      return null;
    }
    if (!response.ok) {
      const detail = responseText.replace(/[\r\n\t]+/g, " ").slice(0, 300);
      throw new IncentivosApiError(
        `La API de Incentivos respondió HTTP ${response.status}${detail ? `: ${detail}` : ""}`,
        response.status,
      );
    }

    if (!responseText.trim()) {
      return null;
    }
    try {
      return JSON.parse(responseText);
    } catch {
      return responseText;
    }
  };
}
