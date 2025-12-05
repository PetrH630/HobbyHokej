const API_URL = 'http://localhost:8080'; // Spring Boot backend

export const getMatches = async () => {
    const response = await fetch(`${API_URL}/api/matches`);
    return await response.json();
};