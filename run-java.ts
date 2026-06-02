import { execSync } from 'child_process';

try {
    const javaVersion = execSync('java -version 2>&1').toString();
    console.log('Java is installed:\n', javaVersion);
    
    // Let's also check if keytool is available under a standard path
    const keytoolPath = execSync('which keytool 2>&1 || find /usr -name keytool 2>&1').toString();
    console.log('Keytool search result:\n', keytoolPath);
} catch (e: any) {
    console.log('Java is not directly in PATH. Error:', e.message);
}
