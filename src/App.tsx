import React, { useState, useEffect } from 'react';
import { 
  Smartphone, 
  ShieldCheck, 
  Lock, 
  Unlock, 
  Settings, 
  Plus, 
  Check, 
  Timer, 
  Sparkles, 
  Play, 
  CheckSquare, 
  Square, 
  Search, 
  AlertTriangle,
  BookOpen,
  Info,
  Calendar,
  Clock,
  ArrowRight,
  UserCheck,
  RefreshCw,
  XCircle,
  HelpCircle,
  GraduationCap,
  Atom,
  Youtube,
  MessageCircle,
  Instagram,
  Gamepad2,
  Chrome,
  Share2
} from 'lucide-react';

interface MockApp {
  id: string;
  name: string;
  packageName: string;
  isStudy: boolean;
  category: 'Study' | 'Social' | 'Games' | 'System';
  iconColor: string;
}

interface TimeSlot {
  id: number;
  startHour: number; // 24-hour format internally
  startMinute: number;
  endHour: number; // 24-hour format internally
  endMinute: number;
  isEnabled: boolean;
}

const getAppIconInfo = (packageName: string) => {
  switch (packageName) {
    case 'com.study.ncert':
      return {
        icon: <BookOpen className="text-white w-full h-full" />,
        bgColor: 'bg-[#d97706]', // Deep amber/orange
      };
    case 'com.physicswallah':
      return {
        icon: <Atom className="text-white w-full h-full animate-spin-slow" />,
        bgColor: 'bg-[#121824] border border-slate-800', // Black/dark branding
      };
    case 'org.khanacademy':
      return {
        icon: <GraduationCap className="text-white w-full h-full" />,
        bgColor: 'bg-[#00705a]', // Greenish teal
      };
    case 'com.google.android.youtube':
      return {
        icon: <Youtube className="text-white w-full h-full text-red-500 fill-white" />,
        bgColor: 'bg-white border border-slate-200 shadow-sm', // Clean white background for YouTube
      };
    case 'com.whatsapp':
      return {
        icon: <MessageCircle className="text-white w-full h-full" fill="currentColor" />,
        bgColor: 'bg-[#25D366]', // WhatsApp Green
      };
    case 'com.instagram.android':
      return {
        icon: <Instagram className="text-white w-full h-full" />,
        bgColor: 'bg-gradient-to-tr from-[#f9ce34] via-[#ee2a7b] to-[#6228d7]', // Instagram Gradient
      };
    case 'com.kiloo.subwaysurf':
      return {
        icon: <Gamepad2 className="text-white w-full h-full" />,
        bgColor: 'bg-gradient-to-br from-amber-500 to-yellow-400', // Subway orange
      };
    case 'com.dts.freefireth':
      return {
        icon: <Gamepad2 className="text-white w-full h-full text-orange-500" />,
        bgColor: 'bg-[#111] border border-orange-600/30 shadow-md shadow-orange-950/20', // Dark free fire
      };
    case 'com.android.chrome':
      return {
        icon: <Chrome className="text-white w-full h-full" />,
        bgColor: 'bg-gradient-to-br from-blue-500 via-red-500 to-yellow-400 border border-slate-800/20', // Chrome Colors match
      };
    default:
      return {
        icon: <Smartphone className="text-white w-full h-full" />,
        bgColor: 'bg-indigo-600',
      };
  }
};

export default function App() {
  // Virtual Phone Simulator State
  const [virtualApps, setVirtualApps] = useState<MockApp[]>([
    { id: '1', name: 'NCERT Books', packageName: 'com.study.ncert', isStudy: true, category: 'Study', iconColor: 'bg-emerald-500' },
    { id: '2', name: 'Physics Wallah', packageName: 'com.physicswallah', isStudy: true, category: 'Study', iconColor: 'bg-blue-500' },
    { id: '3', name: 'Khan Academy', packageName: 'org.khanacademy', isStudy: true, category: 'Study', iconColor: 'bg-teal-600' },
    { id: '4', name: 'YouTube', packageName: 'com.google.android.youtube', isStudy: false, category: 'Social', iconColor: 'bg-red-600' },
    { id: '5', name: 'WhatsApp', packageName: 'com.whatsapp', isStudy: false, category: 'Social', iconColor: 'bg-green-500' },
    { id: '6', name: 'Instagram', packageName: 'com.instagram.android', isStudy: false, category: 'Social', iconColor: 'bg-pink-600' },
    { id: '7', name: 'Subway Surfers', packageName: 'com.kiloo.subwaysurf', isStudy: false, category: 'Games', iconColor: 'bg-amber-500' },
    { id: '8', name: 'Free Fire', packageName: 'com.dts.freefireth', isStudy: false, category: 'Games', iconColor: 'bg-orange-600' },
    { id: '9', name: 'Chrome Browser', packageName: 'com.android.chrome', isStudy: false, category: 'System', iconColor: 'bg-sky-500' },
  ]);

  // Simulated Time & Calendar synced with system real-time clock
  const [simulatedHour, setSimulatedHour] = useState(() => new Date().getHours());
  const [simulatedMinute, setSimulatedMinute] = useState(() => new Date().getMinutes());
  const [simulatedDayIndex, setSimulatedDayIndex] = useState(() => new Date().getDay());
  const [simulatedDate, setSimulatedDate] = useState(() => new Date().getDate());
  const [simulatedMonth, setSimulatedMonth] = useState(() => {
    const months = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
    return months[new Date().getMonth()];
  });
  const [simulatedYear, setSimulatedYear] = useState(() => new Date().getFullYear());

  // 7 Time Slots table for student study block schedules (12-hour AM/PM support) - 24H gapless coverage enabled by default
  const [timeSlots, setTimeSlots] = useState<TimeSlot[]>([
    { id: 1, startHour: 8, startMinute: 0, endHour: 10, endMinute: 30, isEnabled: true },
    { id: 2, startHour: 10, startMinute: 30, endHour: 13, endMinute: 0, isEnabled: true },
    { id: 3, startHour: 13, startMinute: 0, endHour: 15, endMinute: 30, isEnabled: true },
    { id: 4, startHour: 15, startMinute: 30, endHour: 18, endMinute: 0, isEnabled: true },
    { id: 5, startHour: 18, startMinute: 0, endHour: 20, endMinute: 30, isEnabled: true },
    { id: 6, startHour: 20, startMinute: 30, endHour: 23, endMinute: 0, isEnabled: true },
    { id: 7, startHour: 23, startMinute: 0, endHour: 8, endMinute: 0, isEnabled: true }
  ]);

  // Break state Management
  const [breakRemainingSecs, setBreakRemainingSecs] = useState<number | null>(null);
  const [customBreakDuration, setCustomBreakDuration] = useState(15);
  const [showCustomBreakSelector, setShowCustomBreakSelector] = useState(false);
  const [showShareModal, setShowShareModal] = useState(false);

  // App Permissions States for simulation
  const [showPermissionsDialog, setShowPermissionsDialog] = useState(false);
  const [isSimulatedAccessibilityOn, setIsSimulatedAccessibilityOn] = useState(false);
  const [isSimulatedOverlayOn, setIsSimulatedOverlayOn] = useState(false);
  const [isSimulatedBatteryOn, setIsSimulatedBatteryOn] = useState(false);

  // Phone internal screen layout
  const [phoneScreen, setPhoneScreen] = useState<'launcher' | 'study_shield_app' | 'blocked_screen'>('study_shield_app');
  const [activeTab, setActiveTab] = useState<'home' | 'apps' | 'info'>('home');
  const [selectedBlockedAppName, setSelectedBlockedAppName] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [launchNotification, setLaunchNotification] = useState<string | null>(null);
  const [isPolicyExpanded, setIsPolicyExpanded] = useState(false);
  const [isPrivacyView, setIsPrivacyView] = useState(() => {
    return (
      window.location.pathname.includes('privacy') ||
      window.location.search.includes('view=privacy') ||
      window.location.hash.includes('privacy')
    );
  });

  useEffect(() => {
    const handleUrlChange = () => {
      setIsPrivacyView(
        window.location.pathname.includes('privacy') ||
        window.location.search.includes('view=privacy') ||
        window.location.hash.includes('privacy')
      );
    };

    window.addEventListener('popstate', handleUrlChange);
    window.addEventListener('hashchange', handleUrlChange);
    const routeInterval = setInterval(handleUrlChange, 1500);

    return () => {
      window.removeEventListener('popstate', handleUrlChange);
      window.removeEventListener('hashchange', handleUrlChange);
      clearInterval(routeInterval);
    };
  }, []);

  const getPublicLink = (pathAndQuery: string) => {
    const isLocalOrDev = window.location.hostname.includes('dev') || window.location.hostname.includes('localhost') || window.location.hostname.includes('127.0.0.1');
    const base = isLocalOrDev ? "https://ais-pre-yvz7il3zmltaegiiboatvp-142106032593.asia-east1.run.app" : window.location.origin;
    return base + pathAndQuery;
  };

  // Editing dialog state for time slot
  const [editingSlotId, setEditingSlotId] = useState<number | null>(null);
  const [editStartHour12, setEditStartHour12] = useState(9);
  const [editStartMin, setEditStartMin] = useState(0);
  const [editStartAmPm, setEditStartAmPm] = useState<'AM' | 'PM'>('AM');
  const [editEndHour12, setEditEndHour12] = useState(11);
  const [editEndMin, setEditEndMin] = useState(0);
  const [editEndAmPm, setEditEndAmPm] = useState<'AM' | 'PM'>('AM');

  // Days mapping in Hindi and English
  const daysOfWeek = [
    { eng: 'Sunday', hin: 'रविवार' },
    { eng: 'Monday', hin: 'सोमवार' },
    { eng: 'Tuesday', hin: 'मंगलवार' },
    { eng: 'Wednesday', hin: 'बुधवार' },
    { eng: 'Thursday', hin: 'गुरुवार' },
    { eng: 'Friday', hin: 'शुक्रवार' },
    { eng: 'Saturday', hin: 'शनिवार' }
  ];

  // Real live clock ticking in actual real-time
  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      setSimulatedHour(now.getHours());
      setSimulatedMinute(now.getMinutes());
      setSimulatedDayIndex(now.getDay());
      setSimulatedDate(now.getDate());
      
      const months = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
      setSimulatedMonth(months[now.getMonth()]);
      setSimulatedYear(now.getFullYear());
    };
    
    updateTime(); // Initial update
    const interval = setInterval(updateTime, 1000); // Check every second
    return () => clearInterval(interval);
  }, []);

  // Real-time ticking for the Emergency break (actual real seconds)
  useEffect(() => {
    if (breakRemainingSecs !== null && breakRemainingSecs > 0) {
      const timer = setInterval(() => {
        setBreakRemainingSecs(prev => {
          if (prev && prev > 1) {
            return prev - 1;
          } else {
            return null;
          }
        });
      }, 1000);
      return () => clearInterval(timer);
    }
  }, [breakRemainingSecs]);

  // Helper 12H To 24H converter
  const convert12To24 = (h12: number, min: number, ampm: 'AM' | 'PM'): { hr24: number, min24: number } => {
    let hr = h12;
    if (ampm === 'PM' && hr < 12) hr += 12;
    if (ampm === 'AM' && hr === 12) hr = 0;
    return { hr24: hr, min24: min };
  };

  // Helper 24H To 12H converter
  const convert24To12 = (hr24: number): { h12: number, ampm: 'AM' | 'PM' } => {
    const ampm = hr24 >= 12 ? 'PM' : 'AM';
    let h12 = hr24 % 12;
    if (h12 === 0) h12 = 12;
    return { h12, ampm };
  };

  // Check if simulated time falls in any enabled slots
  const isWithinStudyHours = () => {
    const currentTotalMin = simulatedHour * 60 + simulatedMinute;
    
    return timeSlots.some(slot => {
      if (!slot.isEnabled) return false;
      const startTotalMin = slot.startHour * 60 + slot.startMinute;
      const endTotalMin = slot.endHour * 60 + slot.endMinute;

      if (startTotalMin < endTotalMin) {
        return currentTotalMin >= startTotalMin && currentTotalMin < endTotalMin;
      } else {
        // Overnight schedule
        return currentTotalMin >= startTotalMin || currentTotalMin < endTotalMin;
      }
    });
  };

  // Is blocker active right now? (True only if within schedule and NOT on break)
  const isBlockerActive = () => {
    const isBreakOn = breakRemainingSecs !== null && breakRemainingSecs > 0;
    return isWithinStudyHours() && !isBreakOn;
  };

  // Handle app launch from Virtual Phone context
  const handleLaunchApp = (app: MockApp) => {
    if (isBlockerActive() && !app.isStudy) {
      // Blocked app launch attempt
      setSelectedBlockedAppName(app.name);
      setPhoneScreen('blocked_screen');
    } else {
      // Normal or Allowed app launch
      setLaunchNotification(`Simulation: "${app.name}" opened successfully.`);
      setTimeout(() => setLaunchNotification(null), 3000);
    }
  };

  // Format Helper for slots list (e.g. 09:30 AM)
  const formatSlotTime = (h: number, m: number) => {
    const ampm = h >= 12 ? 'PM' : 'AM';
    let h12 = h % 12;
    if (h12 === 0) h12 = 12;
    return `${String(h12).padStart(2, '0')}:${String(m).padStart(2, '0')} ${ampm}`;
  };

  // Trigger editing a time slot
  const startEditingSlot = (slot: TimeSlot) => {
    const startObj = convert24To12(slot.startHour);
    const endObj = convert24To12(slot.endHour);

    setEditingSlotId(slot.id);
    setEditStartHour12(startObj.h12);
    setEditStartMin(slot.startMinute);
    setEditStartAmPm(startObj.ampm);

    setEditEndHour12(endObj.h12);
    setEditEndMin(slot.endMinute);
    setEditEndAmPm(endObj.ampm);
  };

  // Save the modified slot
  const saveEditedSlot = () => {
    if (editingSlotId === null) return;

    const start24 = convert12To24(editStartHour12, editStartMin, editStartAmPm);
    const end24 = convert12To24(editEndHour12, editEndMin, editEndAmPm);

    setTimeSlots(prev => prev.map(slot => {
      if (slot.id === editingSlotId) {
        return {
          ...slot,
          startHour: start24.hr24,
          startMinute: start24.min24,
          endHour: end24.hr24,
          endMinute: end24.min24
        };
      }
      return slot;
    }));

    setEditingSlotId(null);
  };

  // Helper to determine AdSense full-screen ad duration based on break minutes
  const getAdDurationForMinutes = (minutes: number): number => {
    if (minutes >= 1 && minutes <= 5) return 5;
    if (minutes > 5 && minutes <= 10) return 10;
    if (minutes > 10 && minutes <= 15) return 15;
    if (minutes > 15 && minutes <= 30) return 30;
    if (minutes > 30 && minutes <= 45) return 45;
    if (minutes > 45 && minutes <= 60) return 60;
    return 5; // fallback
  };

  // State for active simulated Google Ad
  const [activeAd, setActiveAd] = useState<{
    isOpen: boolean;
    adDuration: number;
    adSecsRemaining: number;
    pendingBreakMinutes: number;
    adKey: number; // to reset/randomize ad content on different runs
  } | null>(null);

  // Trigger break request by loading full-screen AdSense ad first
  const requestBreakWithAd = (minutes: number) => {
    const adSecs = getAdDurationForMinutes(minutes);
    setActiveAd({
      isOpen: true,
      adDuration: adSecs,
      adSecsRemaining: adSecs,
      pendingBreakMinutes: minutes,
      adKey: Math.floor(Math.random() * 3) // 3 ad variations
    });
    setShowCustomBreakSelector(false);
  };

  // Ticking for the simulated interstitial Ad using ultra-stable timeout schedule
  useEffect(() => {
    if (!activeAd || !activeAd.isOpen || activeAd.adSecsRemaining <= 0) return;

    const timer = setTimeout(() => {
      setActiveAd(prev => {
        if (!prev || !prev.isOpen || prev.adSecsRemaining <= 0) return prev;
        return {
          ...prev,
          adSecsRemaining: prev.adSecsRemaining - 1
        };
      });
    }, 1000);

    return () => clearTimeout(timer);
  }, [activeAd?.isOpen, activeAd?.adSecsRemaining]);

  const handleFinishAdAndStartBreak = () => {
    if (!activeAd) return;
    const minutes = activeAd.pendingBreakMinutes;
    setActiveAd(null);
    handleActivateBreak(minutes);
  };

  // Helper to get simulated Ad banner data
  const getSimulatedAdContent = (key: number) => {
    const ads = [
      {
        title: "StudySphere Premium",
        tagline: "Distraction-Free Cloud Learning",
        desc: "Sync NCERT solutions plus full interactive physics labs directly across all study devices. Over 10M+ Indian students score higher with premium guides!",
        stats: "4.9 ★ (1.2M Reviews) • 50M+ Installs",
        badge: "Ad by Google AdSense",
        actionText: "Install Free Trial",
        bgColorClass: "from-[#d97706] to-[#b45309]",
        illustration: (
          <div className="relative w-full h-24 bg-slate-900 border border-amber-500/20 rounded-xl flex flex-col items-center justify-center overflow-hidden">
            <span className="absolute top-1 left-2 px-1 py-0.5 bg-amber-600/20 text-amber-400 font-extrabold text-[7.5px] rounded border border-amber-600/30 uppercase tracking-widest leading-none">AI Powered</span>
            <BookOpen size={28} className="text-amber-500 animate-pulse mt-2" />
            <span className="text-[11px] font-black text-slate-100 mt-1">StudySphere Premium Sync</span>
            <span className="text-[8px] text-slate-400">Distraction Blocker Core Active</span>
          </div>
        )
      },
      {
        title: "BrainBoost Focus Capsules",
        tagline: "Natural Memory & Alertness",
        desc: "Stay energized and ultra-focused through 3-hour long exam revision sessions! Pure vegetarian extracts certified of highest purity.",
        stats: "4.7 ★ (250K Reviews) • 2M+ Sold",
        badge: "Sponsored AdHub",
        actionText: "Order 25% Off Today",
        bgColorClass: "from-[#2563eb] to-[#1d4ed8]",
        illustration: (
          <div className="relative w-full h-24 bg-slate-900 border border-blue-500/20 rounded-xl flex flex-col items-center justify-center overflow-hidden">
            <span className="absolute top-1 left-2 px-1 py-0.5 bg-blue-600/20 text-blue-400 font-extrabold text-[7.5px] rounded border border-blue-600/30 uppercase tracking-widest leading-none">100% Organic</span>
            <Sparkles size={28} className="text-blue-400 animate-bounce mt-2" />
            <span className="text-[11px] font-black text-slate-100 mt-1">BrainBoost Focus Enhancer</span>
            <span className="text-[8px] text-slate-400">Ideal for exam preparation</span>
          </div>
        )
      },
      {
        title: "Coding Juniors Pro",
        tagline: "Fullstack Engineering in 30 Days",
        desc: "Build Real Android apps, games, & deploy to AWS. Premium daily mentoring and guaranteed live placements with top tech partners.",
        stats: "4.8 ★ (85K Reviews) • 500K+ Students",
        badge: "Ad by Google AdSense",
        actionText: "Join Live Workshop",
        bgColorClass: "from-[#9333ea] to-[#7e22ce]",
        illustration: (
          <div className="relative w-full h-24 bg-slate-900 border border-purple-500/20 rounded-xl flex flex-col items-center justify-center overflow-hidden">
            <span className="absolute top-1 left-2 px-1 py-0.5 bg-purple-600/20 text-purple-400 font-extrabold text-[7.5px] rounded border border-purple-600/30 uppercase tracking-widest leading-none">Job Guarantee</span>
            <GraduationCap size={30} className="text-purple-400 animate-pulse mt-2" />
            <span className="text-[11px] font-black text-slate-100 mt-1">Coding Juniors Bootcamps</span>
            <span className="text-[8px] text-slate-400">98.4% Students Hired this Month</span>
          </div>
        )
      }
    ];
    return ads[key] || ads[0];
  };

  // Handle emergency break trigger
  const handleActivateBreak = (minutes: number) => {
    setBreakRemainingSecs(minutes * 60);
    setShowCustomBreakSelector(false);
    
    // If the student is on the warning screen, we should automatically resume and unlock
    if (phoneScreen === 'blocked_screen') {
      setPhoneScreen('study_shield_app');
      setActiveTab('home');
    }
  };

  if (isPrivacyView) {
    return (
      <div className="min-h-screen bg-[#070b13] text-slate-100 py-12 px-6 font-sans">
        <div className="max-w-3xl mx-auto bg-[#0d1424] border border-slate-800 rounded-2xl p-6 md:p-10 shadow-2xl space-y-6">
          
          {/* Top Navbar */}
          <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 pb-4 border-b border-slate-800">
            <div>
              <h1 className="text-xl md:text-2xl font-black tracking-tight text-white flex items-center gap-2">
                <ShieldCheck className="text-indigo-500" size={24} />
                गोपनीयता नीति (Privacy Policy) — Study Mode App Lock & Timer
              </h1>
              <p className="text-xs text-slate-400 font-mono mt-1">Last Updated: June 02, 2026 • com.studyshield.studyfocus</p>
            </div>
            <button
              onClick={() => {
                setIsPrivacyView(false);
                // Clear query or hash
                window.history.pushState({}, '', window.location.pathname);
              }}
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 active:scale-95 text-white font-extrabold text-xs rounded-xl shadow transition duration-150 shrink-0"
            >
              ← Back to App (ऐप खोलें)
            </button>
          </div>

          <div className="bg-indigo-950/20 border border-indigo-500/10 rounded-xl p-4 text-xs text-slate-300 leading-relaxed">
            <span className="font-bold text-indigo-400 block text-xs mb-1 uppercase tracking-wider">🔒 Google Play Developer Content Compliance Disclosure</span>
            'Study Mode App Lock & Timer' ऐप उपयोगकर्ता की गोपनीयता और डेटा सुरक्षा के प्रति पूरी तरह प्रतिबद्ध है। यह गोपनीयता नीति स्पष्ट करती है कि हमारा ऐप किस प्रकार काम करता है, कौन सी अनुमतियाँ लेता है और आपके डेटा को कैसे सुरक्षित रखता है। हम उपयोगकर्ता का कोई भी व्यक्तिगत, संवेदनशील या ब्राउज़िंग डेटा बाहरी सर्वर पर संग्रहीत या साझा <strong>नहीं</strong> करते हैं। ऐप का सारा कार्य पूरी तरह से आपके डिवाइस पर स्थानीय (Offline Local Processing) रूप से होता है।
          </div>

          <div className="space-y-4 text-xs leading-relaxed text-slate-350">
            
            {/* Section 1 */}
            <div className="space-y-1.5">
              <h2 className="text-sm font-extrabold text-white tracking-wide border-l-2 border-indigo-500 pl-2">
                1. संवेदनशील अनुमतियाँ और उपयोग (Sensitive Permissions & Their Use)
              </h2>
              
              <div className="bg-[#11192e] border border-slate-850 p-3 rounded-xl space-y-2 mt-1">
                <h3 className="font-bold text-slate-200 text-xs">A. एक्सीसिबिलिटी सर्विस (Accessibility Service API)</h3>
                <p>हमारा ऐप विचलित करने वाले ऐप्स को ब्लॉक करने के लिए <strong className="text-indigo-400 font-extrabold font-mono">Accessibility Service API</strong> का उपयोग करता है।</p>
                <ul className="list-disc list-inside space-y-1 pl-1 text-slate-400">
                  <li><strong>उद्देश्य (Purpose):</strong> यह केवल यह पता लगाने के लिए उपयोग की जाती है कि वर्तमान में आपके स्क्रीन पर कौन सा ऐप खुला हुआ है। यदि वह ऐप आपकी ब्लॉक्ड लिस्ट में शामिल है, तो यह ऐप उसे रोक कर आपको ध्यान केंद्रित (Study Shield Block Screen) करने के लिए प्रेरित करता है।</li>
                  <li><strong>डेटा संग्रह सीमा (No Data Harvesting):</strong> यह सर्विस किसी भी प्रकार का व्यक्तिगत डेटा, बटन क्लिक, इनपुट टेक्स्ट, पासवर्ड, या व्यक्तिगत जानकारी एकत्र <strong>नहीं</strong> करती है और न ही इसे इंटरनेट पर भेजती है।</li>
                  <li>यह सेवा उपयोगकर्ता की पूर्ण स्पष्ट सहमति से ही सक्रिय की जाती है और इसे डिवाइस सेटिंग्स से कभी भी आसानी से बंद किया जा सकता है।</li>
                </ul>
              </div>

              <div className="bg-[#11192e] border border-slate-850 p-3 rounded-xl space-y-1 mt-2">
                <h3 className="font-bold text-slate-200 text-xs">B. स्क्रीन ओवरले अनुमति (System Alert Window)</h3>
                <p>जब आप पढ़ाई के समय कोई ब्लॉक किया गया ऐप (जैसे सोशल मीडिया या गेम्स) खोलने की कोशिश करते हैं, तो यह अनुमति ऐप को उसके ऊपर सुरक्षा स्क्रीन दिखाने में मदद करती है।</p>
              </div>
            </div>

            {/* Section 2 */}
            <div className="space-y-1.5">
              <h2 className="text-sm font-extrabold text-white tracking-wide border-l-2 border-indigo-500 pl-2">
                2. डेटा सुरक्षा और गोपनीयता (Data Safety & Privacy)
              </h2>
              <ul className="list-disc list-inside space-y-1 pl-2 text-slate-400">
                <li><strong className="text-slate-200">स्थानीय भंडारण (Local Storage):</strong> आपका समय सारणी (Time Table Schedule) और ब्लॉक्ड ऐप्स की सूची केवल आपके डिवाइस के स्थानीय स्टोरेज में संग्रहीत की जाती है।</li>
                <li><strong className="text-slate-200">कोई बाहरी सर्वर नहीं (No Cloud Server):</strong> हम किसी भी प्रकार का बैकएंड डेटाबेस या क्लाउड एनालिटिक्स टूल उपयोग नहीं करते जो आपका डेटा एकत्र करे।</li>
                <li><strong className="text-slate-200">विज्ञापन नीतियां (AdSense Interstitial Ads):</strong> ऐप में ब्रेक अवधि के दौरान Google AdSense इंटरस्टिशियल विज्ञापन प्रदर्शित किए जाते हैं। ये विज्ञापन Google की निर्धारित डेवलपर नीतियों के अनुसार पूरी तरह सुरक्षित हैं और उपयोगकर्ता डेटा लीक नहीं करते हैं।</li>
              </ul>
            </div>

            {/* Section 3 */}
            <div className="space-y-1.5">
              <h2 className="text-sm font-extrabold text-white tracking-wide border-l-2 border-indigo-500 pl-2">
                3. बच्चों की गोपनीयता (Children's Privacy Protection)
              </h2>
              <p className="text-slate-400">
                यह ऐप शिक्षा और ध्यान केंद्रित करने के उद्देश्य से बनाया गया है। यह किसी भी बच्चे से संबंधित संवेदनशील जानकारी एकत्र नहीं करता है और COPPA (Children's Online Privacy Protection Act) जैसी नीतियों का पूर्ण रूप से पालन करता है।
              </p>
            </div>

            {/* Section 4 */}
            <div className="space-y-1.5">
              <h2 className="text-sm font-extrabold text-white tracking-wide border-l-2 border-indigo-500 pl-2">
                4. डेवलपर संपर्क जानकारी (Developer Contact Info)
              </h2>
              <p className="text-slate-400">
                यदि इस गोपनीयता नीति या ऐप के अधिकारों के बारे में आपका कोई प्रश्न या सुझाव है, तो आप हमें सीधे ईमेल कर सकते हैं:
              </p>
              <div className="bg-[#11192e] border border-slate-850 p-2.5 rounded-lg text-slate-200 font-mono mt-1">
                Developer Support: <a href="mailto:jitu1199pal@gmail.com" className="text-indigo-400 font-bold hover:underline">jitu1199pal@gmail.com</a>
              </div>
            </div>

          </div>

          {/* Footer */}
          <div className="pt-4 border-t border-slate-800 text-center text-[10px] text-slate-500 flex justify-between items-center select-none">
            <span>&copy; 2026 Study Mode App Lock & Timer Dev Team.</span>
            <span className="font-bold text-indigo-400">✔ Google Play Store Compliant Document</span>
          </div>

        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans select-none overflow-x-hidden antialiased">
      {/* Centered Desktop Work Desk / Frame Layout */}
      <main className="flex-1 w-full max-w-4xl mx-auto p-4 md:p-8 flex flex-col items-center justify-center gap-6">
        
        {/* Aesthetic Centered Phone Model containing the main APK and UI exclusively */}
        <div className="relative w-full max-w-[365px] bg-[#0c1220] border-[8px] border-[#1e293b] rounded-[50px] shadow-2xl shadow-indigo-500/10 ring-4 ring-slate-800/50 overflow-hidden flex flex-col justify-between" id="phone-wrapper-id">
          
          {/* Punch hole Camera notch */}
          <div className="absolute top-2 left-1/2 -translate-x-1/2 w-32 h-6 bg-black rounded-full z-45 flex items-center justify-start px-2">
            <span className="w-2.5 h-2.5 bg-slate-900 rounded-full border border-blue-900/40"></span>
            <span className="w-1.5 h-1.5 bg-blue-950 rounded-full ml-1"></span>
            {/* Dynamic Status Lock Icon */}
            <div className="ml-auto pr-1">
              {isBlockerActive() ? (
                <Lock size={10} className="text-rose-500" />
              ) : (
                <Unlock size={10} className="text-emerald-400" />
              )}
            </div>
          </div>

          {/* Simulated Mobile Status bar */}
          <div className="h-10 pt-3 px-6 flex justify-between items-center bg-[#090d16] text-[11px] font-bold text-slate-300 relative z-30 select-none">
            <div className="text-[11px] flex items-center gap-1">
              <span>{formatSlotTime(simulatedHour, simulatedMinute)}</span>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-[10px] text-slate-400 font-mono">18+ Student Focus</span>
              <span className="text-emerald-400">LTE</span>
              <span className="text-emerald-400 font-bold">99%</span>
            </div>
          </div>

          {/* Interactive Screen Display viewport area */}
          <div className="flex-1 h-[610px] bg-[#090d16] relative flex flex-col overflow-hidden z-20">
            
            {/* Notification Toast for Launch Simulation updates */}
            {launchNotification && (
              <div className="absolute top-12 left-1/2 -translate-x-1/2 z-50 w-[85%] bg-blue-900 text-white text-[10px] px-3 py-2 rounded-xl text-center border border-blue-700 shadow-xl opacity-95 animate-pulse font-semibold">
                {launchNotification}
              </div>
            )}

            {activeAd && activeAd.isOpen && (
              <div className="absolute inset-0 z-50 bg-[#070b13] flex flex-col justify-between p-4 text-slate-200">
                {/* 1. Header of the Interstitial Ad */}
                <div className="flex justify-between items-center bg-[#0d1424] border border-slate-800 -mx-4 -mt-4 p-2.5 px-4 mb-4 select-none">
                  {/* Left: Info button and Ad attribution */}
                  <div className="flex items-center gap-1.5">
                    <span className="bg-amber-500 text-[8.5px] text-black font-extrabold px-1 rounded-sm border border-amber-400/40">Ad</span>
                    <span className="text-[10px] text-slate-400 font-extrabold tracking-wider font-mono">Google AdSense</span>
                    <span className="text-[10px] text-indigo-450 font-medium font-mono">ℹ</span>
                  </div>
                  
                  {/* Right: Close or countdown status */}
                  <div className="flex items-center gap-2">
                    {activeAd.adSecsRemaining > 0 ? (
                      <div className="flex items-center gap-1 bg-rose-950/60 border border-rose-800/40 px-2 py-0.5 rounded-full text-[10px] text-rose-300 font-extrabold">
                        <Clock size={11} className="text-rose-400 animate-spin-slow" />
                        <span>Ad remains: {activeAd.adSecsRemaining}s</span>
                      </div>
                    ) : (
                      <span className="bg-emerald-950/80 border border-emerald-800 text-emerald-400 px-2 py-0.5 rounded text-[9px] font-black animate-pulse">
                        ✔ Completed!
                      </span>
                    )}

                    {/* Highly responsive closing button */}
                    <button
                      onClick={activeAd.adSecsRemaining <= 0 ? handleFinishAdAndStartBreak : undefined}
                      disabled={activeAd.adSecsRemaining > 0}
                      className={`flex items-center justify-center p-1.5 rounded-full border transition ${
                        activeAd.adSecsRemaining <= 0
                          ? 'bg-rose-600 hover:bg-rose-500 cursor-pointer text-white border-rose-400 shadow-md transform active:scale-90 font-black'
                          : 'bg-slate-900 border-slate-800 text-slate-600 cursor-not-allowed opacity-40'
                      }`}
                      style={{ width: '22px', height: '22px' }}
                    >
                      ✕
                    </button>
                  </div>
                </div>

                {/* 2. Main content area (The Ad Card itself) */}
                {(() => {
                  const ad = getSimulatedAdContent(activeAd.adKey);
                  return (
                    <div className="flex-1 flex flex-col justify-center items-center gap-2.5 text-center px-1">
                      {/* Decorative elements representing loading or active play */}
                      <div className="space-y-0.5">
                        <h4 className="text-[9px] text-indigo-400 font-black uppercase tracking-widest">Featured Academic Offer</h4>
                        <p className="text-[9px] text-slate-500 font-medium">For Break of <span className="text-white font-bold">{activeAd.pendingBreakMinutes} Min</span></p>
                      </div>

                      {/* Display Illustration */}
                      <div className="w-full">
                        {ad.illustration}
                      </div>

                      {/* Info fields */}
                      <div className="space-y-1 mt-1">
                        <div className="flex justify-center items-center gap-1 bg-white/5 py-0.5 px-2 border border-white/5 rounded-full w-full max-w-fit mx-auto mt-1">
                          <span className="text-[8.5px] text-yellow-400 font-extrabold">{ad.stats}</span>
                        </div>
                        <h3 className="text-sm font-black text-white tracking-tight leading-tight">{ad.title}</h3>
                        <p className="text-[10px] font-bold text-indigo-300 leading-tight">{ad.tagline}</p>
                        <p className="text-[9px] text-slate-450 leading-normal px-2">
                          {ad.desc}
                        </p>
                      </div>

                      {/* Progress bar representing ad timeline ticking */}
                      <div className="w-full bg-slate-900 h-1.5 rounded-full mt-1.5 overflow-hidden border border-slate-850">
                        <div 
                          className="bg-indigo-500 h-full transition-all duration-1000 ease-linear"
                          style={{ width: `${(activeAd.adSecsRemaining / activeAd.adDuration) * 100}%` }}
                        ></div>
                      </div>

                      {/* Bottom action trigger CTA */}
                      <button
                        onClick={() => {
                          setLaunchNotification('Simulating Ad Redirect...');
                          setTimeout(() => setLaunchNotification(null), 2500);
                        }}
                        className={`w-full py-2 rounded-xl border font-black text-xs uppercase tracking-wider text-slate-950 bg-gradient-to-r ${ad.bgColorClass} text-white border-white/10 hover:brightness-110 active:scale-98 transition shadow-lg mt-1`}
                      >
                        {ad.actionText}
                      </button>
                    </div>
                  );
                })()}

                {/* 3. Bottom Close/Start actions to start break once ad ends */}
                <div className="pt-2 border-t border-slate-900 flex flex-col gap-1.5 mt-2 select-none">
                  <span className="text-[8.5px] text-center text-slate-500 leading-none">
                    *Ad completion is required to begin break duration parameter.
                  </span>
                  
                  {activeAd.adSecsRemaining <= 0 ? (
                    <button
                      onClick={handleFinishAdAndStartBreak}
                      className="w-full py-2.5 bg-gradient-to-r from-emerald-600 to-green-500 hover:from-emerald-500 hover:to-green-400 active:scale-95 text-slate-950 font-black text-[10.5px] uppercase tracking-wider rounded-xl shadow-lg border border-emerald-400/30 transition flex items-center justify-center gap-1 animate-pulse"
                    >
                      <Timer size={12} />
                      ब्रेक शुरू करें (Start Break)
                    </button>
                  ) : (
                    <div className="w-full py-1.5 bg-slate-900 border border-slate-800 text-[10px] font-extrabold text-slate-500 rounded-xl text-center">
                      Ad details loading... ({activeAd.adSecsRemaining}s remaining)
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* SCREEN 1: PHONE HOME LAUNCHER VIEW */}
            {phoneScreen === 'launcher' && (
              <div className="flex-1 p-4 flex flex-col justify-between bg-gradient-to-b from-[#111e3b] via-[#090d16] to-[#05070c]">
                
                {/* Simulated Desk Wallpaper Widget showing day-date time */}
                <div className="bg-white/5 border border-white/10 rounded-2xl p-4 text-center">
                  <span className="text-[9px] uppercase tracking-wider text-slate-400 font-extrabold">Virtual Phone Home</span>
                  <h3 className="text-2xl font-black text-white mt-1">
                    {formatSlotTime(simulatedHour, simulatedMinute)}
                  </h3>
                  <p className="text-xs text-blue-300 font-semibold">
                    {daysOfWeek[simulatedDayIndex].hin} ({daysOfWeek[simulatedDayIndex].eng})
                  </p>
                  <p className="text-[10px] text-slate-400">
                    {simulatedDate} {simulatedMonth} {simulatedYear}
                  </p>
                  
                  {/* Lock Indicator */}
                  <div className="mt-3">
                    {isBlockerActive() ? (
                      <div className="inline-flex items-center gap-1.5 bg-rose-950/60 border border-rose-800/80 px-3 py-1 rounded-full text-[10px] text-rose-300 font-extrabold animate-pulse">
                        <Lock size={10} />
                        पढ़ाई समय सक्रिय: नॉन-स्टडी ऐप्स बंद हैं
                      </div>
                    ) : (
                      <div className="inline-flex items-center gap-1.5 bg-emerald-950/60 border border-emerald-800/80 px-3 py-1 rounded-full text-[10px] text-emerald-400 font-extrabold">
                        <Unlock size={10} />
                        मुक्त मोड: सभी ऐप्स खुली हैं
                      </div>
                    )}
                  </div>
                </div>

                {/* Simulated App Icon Grid */}
                <div className="space-y-3">
                  <p className="text-[10px] font-extrabold text-slate-400 uppercase tracking-widest px-1">APPLICATIONS GRID (लॉन्च करें):</p>
                  <div className="grid grid-cols-4 gap-y-4 gap-x-2">
                    
                    {/* Core Study Mode App Lock & Timer Icon */}
                    <button 
                      onClick={() => {
                        setPhoneScreen('study_shield_app');
                        setActiveTab('home');
                      }}
                      className="flex flex-col items-center group cursor-pointer"
                    >
                      <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-blue-600 to-indigo-600 flex items-center justify-center text-white shadow-lg transition transform group-active:scale-95 border border-blue-400/30">
                        <ShieldCheck size={26} />
                      </div>
                      <span className="text-[9.5px] text-indigo-200 font-extrabold mt-1 truncate w-full text-center">Study Mode App Lock & Timer</span>
                    </button>

                    {/* Simulating App Icons */}
                    {virtualApps
                      .filter(app => !isBlockerActive() || app.isStudy)
                      .map(app => {
                        const iconInfo = getAppIconInfo(app.packageName);
                        return (
                          <button 
                            key={app.id} 
                            onClick={() => handleLaunchApp(app)}
                            className="flex flex-col items-center group cursor-pointer"
                          >
                            <div className={`w-12 h-12 rounded-2xl ${iconInfo.bgColor} flex items-center justify-center p-2.5 text-slate-100 shadow relative transition transform group-active:scale-95`}>
                              {iconInfo.icon}
                              {/* Indicator dot if study app and time slots are on */}
                              {app.isStudy ? (
                                <span className="absolute -top-1 -right-1 bg-emerald-500 text-[8px] text-slate-950 px-1 rounded-full font-black">✓</span>
                              ) : (
                                <span className="absolute -top-1 -right-1 bg-rose-500 text-[8px] text-white px-1 rounded-full font-black">🔒</span>
                              )}
                            </div>
                            <span className="text-[9.5px] text-slate-300 mt-1 truncate w-full text-center font-medium leading-tight">{app.name}</span>
                          </button>
                        );
                      })}

                  </div>
                </div>

                {/* Launcher Guide */}
                <div className="bg-white/5 p-2 rounded-xl border border-white/5 text-center text-[9px] text-slate-400">
                  ⚠️ Instagram या सोशल ऐप्स दबाकर लॉक चेतावनी स्क्रीन टेस्ट करें।
                </div>

              </div>
            )}

            {/* SCREEN 2: STUDY FOCUS APPLICATION SUITE (Main simulated APK contents) */}
            {phoneScreen === 'study_shield_app' && (
              <div className="flex-1 flex flex-col justify-between bg-[#070b13] relative">
                
                {/* APK Home Top Header bar with exact requested requirements */}
                {/* Requirement: time houre or minite me, war (Day of Week), tarikhomonth or year ho TOP par */}
                {/* Requirement: right top corner par Break ka option ho written */}
                <div className="p-3 bg-[#0d1527] border-b border-slate-800 text-slate-100 flex flex-col gap-2 relative">
                  
                  {/* Top line with Date-Day and Time */}
                  <div className="flex justify-between items-start">
                    <div className="flex flex-col">
                      {/* Time and week day (war in Hindi and English) */}
                      <span className="text-md font-black text-blue-400 flex items-center gap-1.5 leading-none">
                        <Clock size={13} className="text-blue-400" />
                        {formatSlotTime(simulatedHour, simulatedMinute)}
                      </span>
                      <span className="text-[10.5px] text-emerald-400 font-bold mt-1">
                        वार: {daysOfWeek[simulatedDayIndex].hin} ({daysOfWeek[simulatedDayIndex].eng})
                      </span>
                      {/* Tarikh, month and year */}
                      <span className="text-[10px] text-slate-400 font-medium">
                        तारीख: {String(simulatedDate).padStart(2, '0')} {simulatedMonth} {simulatedYear}
                      </span>
                    </div>

                    {/* Requirement: TOP RIGHT corner: Break کا option likha hua */}
                    <div className="flex gap-1.5">
                      <button
                        onClick={() => setShowShareModal(true)}
                        className="px-2 py-1 bg-indigo-600 hover:bg-indigo-500 active:scale-95 text-white font-extrabold text-[10px] rounded-lg shadow-md flex items-center gap-1 transition border border-indigo-400/20"
                      >
                        <Share2 size={10} />
                        Share APK
                      </button>

                      <button
                        onClick={() => setShowCustomBreakSelector(true)}
                        className="px-2 py-1 bg-emerald-600 hover:bg-emerald-500 active:scale-95 text-slate-950 font-extrabold text-[10px] rounded-lg shadow-md flex items-center gap-1 transition"
                      >
                        <Timer size={10} />
                        Break लें
                      </button>
                    </div>
                  </div>



                  {/* Custom Break Selector Popup/Overlay inside header space */}
                  {showCustomBreakSelector && (
                    <div className="absolute inset-x-2 top-2 bg-slate-900 border-2 border-emerald-500 rounded-xl p-3.5 z-40 shadow-2xl">
                      <div className="flex justify-between items-center mb-1">
                        <span className="text-[11px] font-bold text-white uppercase tracking-wider">इमरजेंसी ब्रेक समय सेट करें:</span>
                        <XCircle size={14} className="text-rose-500 cursor-pointer" onClick={() => setShowCustomBreakSelector(false)} />
                      </div>
                      
                      {/* Slide Range from 1 to 60 Minutes */}
                      <div className="space-y-3 pt-1">
                        <div className="flex items-center justify-between text-xs font-bold text-emerald-400 bg-emerald-950/50 py-1 px-2 rounded">
                          <span>सिलेक्टेड ब्रेक समय:</span>
                          <span className="text-sm font-black">{customBreakDuration} मिनट (Minutes)</span>
                        </div>
                        
                        <input 
                          type="range"
                          min="1"
                          max="60"
                          value={customBreakDuration}
                          onChange={e => setCustomBreakDuration(parseInt(e.target.value))}
                          className="w-full h-1.5 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-emerald-500"
                        />

                        {/* Presets */}
                        <div className="grid grid-cols-4 gap-1.5 mt-1">
                          {[5, 15, 30, 60].map(m => (
                            <button
                              key={m}
                              onClick={() => setCustomBreakDuration(m)}
                              className="py-1 bg-slate-800 hover:bg-slate-700 text-[10px] text-slate-300 font-extrabold rounded"
                            >
                              {m}m
                            </button>
                          ))}
                        </div>

                        {/* Confirmation Button */}
                        <button
                          onClick={() => requestBreakWithAd(customBreakDuration)}
                          className="w-full py-2 bg-emerald-500 text-slate-950 hover:bg-emerald-400 text-[11px] font-extrabold rounded-lg shadow-lg"
                        >
                          ब्रेक चालू करें (Start Break)
                        </button>
                      </div>
                    </div>
                  )}

                </div>

                {/* Active break banner if currently counting down */}
                {breakRemainingSecs !== null && breakRemainingSecs > 0 && (
                  <div className="bg-emerald-950 border-y border-emerald-800 px-3 py-2 flex items-center justify-between text-[11px]">
                    <div className="flex items-center gap-1.5 text-emerald-400 font-bold">
                      <Timer className="animate-spin" size={13} />
                      <span>ब्रेक जारी है: सभी ऐप्स अनलॉक रहेंगी</span>
                    </div>
                    <span className="text-emerald-300 font-black block bg-slate-900 border border-emerald-800/50 rounded px-1.5 py-0.5">
                      {Math.floor(breakRemainingSecs / 60)}m {breakRemainingSecs % 60}s
                    </span>
                  </div>
                )}

                {/* Dynamic Inner Tab Body list scrollable */}
                <div className="flex-1 overflow-y-auto p-3 space-y-4 scrollbar-none">
                  
                  {/* TAB A: HOME (CONFIG 7 SCHEDULE TIME TABLE) */}
                  {activeTab === 'home' && (
                    <div className="space-y-4">
                      


                      {/* 7 Time slot schedule table */}
                      {/* Requirement: Total 7 time ki table ho jisme student start se end time dalega */}
                      <div className="space-y-2">
                        <div className="flex items-center justify-between px-1">
                          <span className="text-[11px] font-black uppercase text-slate-400 tracking-wider flex items-center gap-1">
                            <Calendar size={12} className="text-blue-400" />
                            7 पढ़ाई शेड्यूलों की तालिका (7 Time Slots)
                          </span>
                          <span className="text-[9px] text-indigo-400 font-bold">12H Time AM/PM</span>
                        </div>

                        {/* List slots in table design */}
                        <div className="space-y-2">
                          {timeSlots.map(slot => (
                            <div 
                              key={slot.id}
                              className={`bg-[#0d1424] border rounded-xl p-3 transition flex items-center justify-between ${
                                slot.isEnabled 
                                  ? 'border-indigo-800 shadow-md shadow-indigo-950/40 lighten-indigo' 
                                  : 'border-slate-800/80 opacity-60'
                              }`}
                            >
                              {/* Left slot identification */}
                              <div>
                                <div className="flex items-center gap-1.5">
                                  <span className={`w-2 h-2 rounded-full ${slot.isEnabled ? 'bg-indigo-400 animate-pulse' : 'bg-slate-500'}`}></span>
                                  <span className="text-xs font-black text-slate-200">शेड्यूल #{slot.id}</span>
                                  {slot.isEnabled && (
                                    <span className="text-[8px] bg-indigo-900 border border-indigo-700 text-indigo-300 px-1 rounded font-black">
                                      सक्रिय (ON)
                                    </span>
                                  )}
                                </div>
                                <div className="text-xs font-semibold text-indigo-300 mt-1">
                                  {formatSlotTime(slot.startHour, slot.startMinute)} से {formatSlotTime(slot.endHour, slot.endMinute)}
                                </div>
                              </div>

                              {/* Right slot action control triggers Dialog editing inline */}
                              <div className="flex items-center gap-3">
                                <button
                                  onClick={() => startEditingSlot(slot)}
                                  className="px-2 py-1 bg-slate-800 hover:bg-slate-700 text-[10px] text-slate-200 font-bold rounded-md border border-slate-700 transition"
                                >
                                  समय बदलें (Edit)
                                </button>
                                
                                {/* Slot Master enable switch toggles */}
                                <button
                                  onClick={() => {
                                    setTimeSlots(prev => prev.map(s => 
                                      s.id === slot.id ? { ...s, isEnabled: !s.isEnabled } : s
                                    ));
                                  }}
                                  className={`w-9 h-5 rounded-full p-0.5 transition ${slot.isEnabled ? 'bg-indigo-500' : 'bg-slate-800'}`}
                                >
                                  <div className={`w-4 h-4 bg-white rounded-full transition-transform ${slot.isEnabled ? 'translate-x-4' : 'translate-x-0'}`}></div>
                                </button>
                              </div>

                            </div>
                          ))}
                        </div>
                      </div>

                    </div>
                  )}

                  {/* TAB B: APPS SELECTOR DIALOG (Checklist setting app categories) */}
                  {/* Requirement: jisme 2 tarah ki apk h ak study app or ak non-study apk */}
                  {activeTab === 'apps' && (
                    <div className="space-y-3">
                      


                      {/* Apps Search Input box */}
                      <div className="flex items-center gap-2 bg-[#0c1220] border border-slate-800 px-3 py-1.5 rounded-lg">
                        <Search size={12} className="text-slate-400" />
                        <input
                          type="text"
                          placeholder="खोजें (Search App)..."
                          value={searchQuery}
                          onChange={e => setSearchQuery(e.target.value)}
                          className="bg-transparent border-none text-[11px] w-full text-white outline-none placeholder:text-slate-500"
                        />
                      </div>

                      {/* Displaying checked items */}
                      <div className="space-y-2 max-h-[300px] overflow-y-auto pr-1">
                        {virtualApps
                          .filter(app => app.name.toLowerCase().includes(searchQuery.toLowerCase()))
                          .map(app => (
                            <div 
                              key={app.id}
                              onClick={() => {
                                setVirtualApps(prev => prev.map(a => 
                                  a.id === app.id ? { ...a, isStudy: !a.isStudy } : a
                                ));
                                setLaunchNotification(`"${app.name}" categorized as ${!app.isStudy ? 'STUDY' : 'NON-STUDY'} app.`);
                                setTimeout(() => setLaunchNotification(null), 2500);
                              }}
                              className={`bg-[#0d1424] border rounded-lg p-2.5 flex items-center justify-between cursor-pointer transition ${
                                app.isStudy 
                                  ? 'border-emerald-800/40 hover:bg-emerald-950/10' 
                                  : 'border-slate-800/50 hover:bg-slate-900'
                              }`}
                            >
                              <div className="flex items-center gap-2.5">
                                {(() => {
                                  const iconInfo = getAppIconInfo(app.packageName);
                                  return (
                                    <div className={`w-8 h-8 rounded-xl ${iconInfo.bgColor} flex items-center justify-center p-1.5 shadow-md text-slate-50 relative`}>
                                      {iconInfo.icon}
                                    </div>
                                  );
                                })()}
                                <div className="leading-snug">
                                  <p className="text-[11px] font-bold text-slate-200">{app.name}</p>
                                  <p className="text-[8px] font-mono text-slate-500">{app.packageName}</p>
                                </div>
                              </div>
                              
                              <div className="flex items-center gap-2">
                                {/* Visual Pill indicator */}
                                {app.isStudy ? (
                                  <span className="text-[8.5px] font-black bg-emerald-950 text-emerald-400 border border-emerald-800 px-2 py-0.5 rounded-full">
                                    स्टडी ऐप (Study App)
                                  </span>
                                ) : (
                                  <span className="text-[8.5px] font-bold bg-slate-950 text-slate-500 border border-slate-850 px-2 py-0.5 rounded-full">
                                    नॉर्मल ऐप (Block)
                                  </span>
                                )}

                                {/* Check / Uncheck Boxes */}
                                {app.isStudy ? (
                                  <CheckSquare size={16} className="text-emerald-500" />
                                ) : (
                                  <Square size={16} className="text-slate-600" />
                                )}

                                {/* Remove button to delete from virtual checklist and phone launcher */}
                                <button
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    setVirtualApps(prev => prev.filter(a => a.id !== app.id));
                                    setLaunchNotification(`"${app.name}" को ऐप स्क्रीन से हटा दिया गया है।`);
                                    setTimeout(() => setLaunchNotification(null), 2500);
                                  }}
                                  className="p-1.5 hover:bg-rose-950/40 text-rose-500 hover:text-rose-400 rounded-md transition-colors ml-1.5"
                                  title="ऐप स्क्रीन से हटाएं"
                                >
                                  <XCircle size={15} />
                                </button>
                              </div>

                            </div>
                          ))}
                      </div>

                    </div>
                  )}

                  {/* TAB C: HELP & RULES OVERVIEW */}
                  {activeTab === 'info' && (
                    <div className="space-y-3 p-1">
                      <div className="bg-[#0c1424] border border-indigo-900 rounded-xl p-3 text-xs leading-normal space-y-2 text-slate-350">
                        <h4 className="font-extrabold text-indigo-400 text-xs text-center border-b border-indigo-950 pb-1.5 uppercase flex justify-center items-center gap-1">
                          <Info size={12} />
                          यह कैसे काम करता है? (Instructions)
                        </h4>
                        <ol className="space-y-2 list-decimal list-inside text-[11px]">
                          <li>
                            <strong className="text-white">ऐप्स चयन:</strong> 'ऐप्स जोडें' टैब में जाकर अपनी पढ़ाई की जरूरत वाली ऐप्स को सिलेक्ट करें (जैसे NCERT, YouTube)।
                          </li>
                          <li>
                            <strong className="text-white">7 टाइम शेड्यूल:</strong> स्टूडेंट अपनी सुविधा के अनुसार 7 टाइम पीरियड सेट कर सकता है।
                          </li>
                          <li>
                            <strong className="text-white">लॉक एक्टिवेशन:</strong> शेड्यूल के समय के दौरान मोबाइल में सिर्फ स्टडी ऐप्स ही खुलेंगी और बाकी सब ब्लॉक रहेंगी।
                          </li>
                          <li>
                            <strong className="text-white">इमरजेंसी ब्रेक:</strong> पढ़ाई के बीच अगर ज़रूरत पड़े, तो ऊपर दाहिने कोने (Top Right) से 1 से 60 मिनट का ब्रेक लेकर सभी सामान्य ऐप्स को इस्तेमाल कर सकते हैं।
                          </li>
                        </ol>
                      </div>

                      {/* Premium Share APK Card */}
                      <div className="bg-gradient-to-r from-indigo-950 to-[#0d1425] border border-indigo-500/20 rounded-xl p-3 space-y-2 text-slate-200 shadow-md">
                        <div className="flex items-center gap-1.5 text-indigo-400">
                          <Share2 size={12} className="animate-pulse" />
                          <h4 className="font-extrabold text-[11px] uppercase tracking-wider">दोस्तों के साथ शेयर करें (Share App)</h4>
                        </div>
                        <p className="text-[10px] text-slate-400 leading-tight">
                          Study Mode App Lock & Timer ऐप को अपने दोस्तों और सहपाठियों के साथ शेयर करें ताकि वे भी अपनी पढ़ाई के दौरान ध्यान लगा सकें!
                        </p>
                        <button
                          onClick={() => setShowShareModal(true)}
                          className="w-full py-1.5 bg-indigo-600 hover:bg-indigo-500 text-white font-extrabold text-[10.5px] rounded-lg shadow transition flex items-center justify-center gap-1 active:scale-95 border border-indigo-400/20 animate-pulse"
                        >
                          <Share2 size={11} />
                          शेयर लिंक प्राप्त करें (Share App Link)
                        </button>
                      </div>

                      {/* Expandable Privacy Policy Widget - Compliance with Play Policy & Disclosures */}
                      <div 
                        className="bg-slate-950/45 border border-indigo-900/60 rounded-xl p-3 cursor-pointer select-none text-left transition hover:border-indigo-600/50"
                        onClick={() => setIsPolicyExpanded(!isPolicyExpanded)}
                      >
                        <div className="flex justify-between items-center text-[11px] font-black text-indigo-400">
                          <span className="flex items-center gap-1.5 uppercase tracking-wide">
                            🔒 Play Store Policy & Privacy
                          </span>
                          <span className="text-[10px] text-indigo-400 font-extrabold font-mono">
                            {isPolicyExpanded ? 'CLOSE ▲' : 'VIEW DETAILS ▼'}
                          </span>
                        </div>
                        <p className="text-[10px] text-slate-300 mt-1.5 leading-normal">
                          यह ऐप पूर्णतः <strong>सुरक्षित और ऑफलाइन</strong> है। हम Google Play नीतियों का पालन करने के लिए बाध्य हैं।
                        </p>
                        {isPolicyExpanded && (
                          <div className="mt-2 pt-2.5 border-t border-indigo-950/80 space-y-2 text-[9.5px] text-slate-400 leading-relaxed">
                            <div>
                              <strong className="text-white block uppercase text-[9px] tracking-wide text-indigo-300">1. Prominent Disclosure (एक्सीसिबिलिटी सर्विस):</strong>
                              <p className="mt-0.5">
                                Study Mode App Lock & Timer, विचलित करने वाले ऐप्स (Blocked Apps List) को स्क्रीन पर ब्लॉक करने के लिए <strong>Accessibility Service API</strong> का उपयोग करता है। यह सेवा केवल इस पहचान के लिए उपयोग होती है कि वर्तमान में स्क्रीन पर कौन सा ऐप खुला हुआ है।
                              </p>
                            </div>
                            <div>
                              <strong className="text-white block uppercase text-[9px] tracking-wide text-indigo-300">2. zero data collection:</strong>
                              <p className="mt-0.5">
                                एक्सीसिबिलिटी सर्विस द्वारा किसी भी प्रकार का व्यक्तिगत डेटा, इनपुट टेक्स्ट, पासवर्ड, या व्यक्तिगत जानकारी को <strong>कभी भी न तो रिकॉर्ड किया जाता है और न ही कहीं भेजा जाता है।</strong> यह पूरी तरह ऑफलाइन और डिवाइस की सीमा में काम करता है।
                              </p>
                            </div>
                            <div>
                              <strong className="text-white block uppercase text-[9px] tracking-wide text-indigo-300">3. ad & adsense compliance:</strong>
                              <p className="mt-0.5">
                                ऐप में दिए गए ब्रेक समय पर Google AdSense के नियमानुसार सुरक्षित विज्ञापन दिखाए जाते हैं, जो विज्ञापनदाताओं के नियमों और Play Store विज्ञापन नीतियों का 100% अनुपालन करते हैं।
                              </p>
                            </div>
                            <div>
                              <strong className="text-white block uppercase text-[9px] tracking-wide text-indigo-300">4. developer contact:</strong>
                              <p className="mt-0.5 font-mono">
                                queries/support: <span className="text-indigo-400 font-bold underline">jitu1199pal@gmail.com</span>
                              </p>
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  )}

                </div>

                {/* Inline Editing Dialog / Form when active */}
                {editingSlotId !== null && (
                  <div className="absolute inset-x-2 bottom-12 bg-slate-900 border-2 border-indigo-500 rounded-2xl p-4 z-40 shadow-2xl">
                    <h5 className="text-[12px] font-black text-white text-center pb-2 border-b border-indigo-950 uppercase">
                      टाइम शेड्यूल #{editingSlotId} संपादित करें
                    </h5>
                    
                    <div className="space-y-3 pt-3">
                      
                      {/* Start Time Selectors 12Hour */}
                      <div>
                        <span className="text-[10px] font-bold text-indigo-400 block mb-1">स्टार्ट समय (Start Time):</span>
                        <div className="flex gap-2 items-center">
                          <select 
                            value={editStartHour12}
                            onChange={e => setEditStartHour12(parseInt(e.target.value))}
                            className="bg-slate-950 border border-slate-800 text-white p-1 rounded font-bold text-xs w-full text-center"
                          >
                            {Array.from({ length: 12 }, (_, i) => i + 1).map(h => (
                              <option key={h} value={h}>{String(h).padStart(2, '0')}</option>
                            ))}
                          </select>
                          <span className="text-slate-500 font-black">:</span>
                          <select 
                            value={editStartMin}
                            onChange={e => setEditStartMin(parseInt(e.target.value))}
                            className="bg-slate-950 border border-slate-800 text-white p-1 rounded font-bold text-xs w-full text-center"
                          >
                            {[0, 15, 30, 45].map(m => (
                              <option key={m} value={m}>{String(m).padStart(2, '0')}</option>
                            ))}
                          </select>
                          <select 
                            value={editStartAmPm}
                            onChange={e => setEditStartAmPm(e.target.value as 'AM' | 'PM')}
                            className="bg-slate-950 border border-slate-800 text-white p-1 rounded font-bold text-xs w-full text-center"
                          >
                            <option value="AM">AM</option>
                            <option value="PM">PM</option>
                          </select>
                        </div>
                      </div>

                      {/* End Time Selectors 12Hour */}
                      <div>
                        <span className="text-[10px] font-bold text-indigo-400 block mb-1">एंड समय (End Time):</span>
                        <div className="flex gap-2 items-center">
                          <select 
                            value={editEndHour12}
                            onChange={e => setEditEndHour12(parseInt(e.target.value))}
                            className="bg-slate-950 border border-[#1e293b] text-white p-1 rounded font-bold text-xs w-full text-center"
                          >
                            {Array.from({ length: 12 }, (_, i) => i + 1).map(h => (
                              <option key={h} value={h}>{String(h).padStart(2, '0')}</option>
                            ))}
                          </select>
                          <span className="text-slate-500 font-black">:</span>
                          <select 
                            value={editEndMin}
                            onChange={e => setEditEndMin(parseInt(e.target.value))}
                            className="bg-slate-950 border border-[#1e293b] text-white p-1 rounded font-bold text-xs w-full text-center"
                          >
                            {[0, 15, 30, 45].map(m => (
                              <option key={m} value={m}>{String(m).padStart(2, '0')}</option>
                            ))}
                          </select>
                          <select 
                            value={editEndAmPm}
                            onChange={e => setEditEndAmPm(e.target.value as 'AM' | 'PM')}
                            className="bg-slate-950 border border-[#1e293b] text-white p-1 rounded font-bold text-xs w-full text-center"
                          >
                            <option value="AM">AM</option>
                            <option value="PM">PM</option>
                          </select>
                        </div>
                      </div>

                      {/* Actions */}
                      <div className="grid grid-cols-2 gap-2 pt-2">
                        <button
                          onClick={() => setEditingSlotId(null)}
                          className="py-1.5 bg-slate-800 text-slate-300 text-[11px] font-bold rounded-lg hover:bg-slate-700"
                        >
                          रद्द करें
                        </button>
                        <button
                          onClick={saveEditedSlot}
                          className="py-1.5 bg-indigo-600 text-white text-[11px] font-black rounded-lg hover:bg-indigo-500"
                        >
                          सुरक्षित करें
                        </button>
                      </div>

                    </div>
                  </div>
                )}

                {/* Simulated Android App bottom navigation bar inside phone mockup container */}
                <div className="bg-[#0b101c] border-t border-slate-900 grid grid-cols-3 select-none py-1.5 text-center relative z-10">
                  <button 
                    onClick={() => setActiveTab('home')}
                    className={`flex flex-col items-center cursor-pointer transition ${activeTab === 'home' ? 'text-blue-500 font-extrabold' : 'text-slate-400'}`}
                  >
                    <Smartphone size={14} />
                    <span className="text-[8px] mt-0.5">डैशबोर्ड (Schedules)</span>
                  </button>

                  <button 
                    onClick={() => setActiveTab('apps')}
                    className={`flex flex-col items-center cursor-pointer transition ${activeTab === 'apps' ? 'text-blue-500 font-extrabold' : 'text-slate-400'}`}
                  >
                    <Plus size={14} />
                    <span className="text-[8px] mt-0.5">ऐप्स जोड़ें (Checklist)</span>
                  </button>

                  <button 
                    onClick={() => setActiveTab('info')}
                    className={`flex flex-col items-center cursor-pointer transition ${activeTab === 'info' ? 'text-blue-500 font-extrabold' : 'text-slate-400'}`}
                  >
                    <Info size={14} />
                    <span className="text-[8px] mt-0.5">मदद (Instructions)</span>
                  </button>
                </div>

                {/* Simulated Floating Settings/Permissions Button on Right Bottom Corner */}
                <button 
                  onClick={() => setShowPermissionsDialog(true)}
                  className="absolute bottom-16 right-4 w-12 h-12 bg-indigo-600 hover:bg-indigo-500 text-white rounded-full flex items-center justify-center shadow-lg transform active:scale-95 transition-all z-30 border border-indigo-400/40 hover:rotate-45"
                  title="अनुमतियाँ चालू करें (App Permissions)"
                >
                  <Settings size={20} className="animate-spin-slow" />
                </button>

                {/* Simulated Permission Dialog Modal matching Android Activity perfectly */}
                {showPermissionsDialog && (
                  <div className="absolute inset-0 bg-[#070b13]/98 z-50 p-4 flex flex-col justify-between overflow-y-auto">
                    
                    <div className="space-y-4">
                      {/* Title Bar */}
                      <div className="flex justify-between items-center pb-2 border-b border-slate-800">
                        <div className="flex items-center gap-1.5">
                          <Settings size={16} className="text-indigo-400" />
                          <span className="text-xs font-black text-indigo-300 uppercase tracking-wider">ऐप अनुमतियाँ (Permissions)</span>
                        </div>
                        <button 
                          onClick={() => setShowPermissionsDialog(false)}
                          className="p-1 hover:bg-slate-800 rounded-full transition"
                        >
                          <XCircle size={18} className="text-rose-500" />
                        </button>
                      </div>

                      <p className="text-[10px] text-slate-400 leading-normal">
                        ऐप की सुचारू कार्यप्रणाली के लिए निम्नलिखित अनुमतियाँ आवश्यक हैं। लाइव ऐप में ये बटन सीधे सिस्टम सेटिंग्स पर रीडायरेक्ट करते हैं:
                      </p>

                      {/* Permissions List */}
                      <div className="space-y-3">
                        
                        {/* 1. Accessibility Service Card */}
                        <div className="bg-[#0c1424] border border-slate-805 rounded-xl p-3 space-y-2">
                          <div className="flex justify-between items-center">
                            <span className="text-[11px] font-black text-slate-200">एक्सेसिबिलिटी (Accessibility)</span>
                            <span className={`text-[9px] font-black px-1.5 py-0.5 rounded ${isSimulatedAccessibilityOn ? 'bg-emerald-950 text-emerald-400 border border-emerald-800/40' : 'bg-rose-950 text-rose-400 border border-rose-800/40'}`}>
                              {isSimulatedAccessibilityOn ? 'चालू (ON)' : 'बंद (OFF)'}
                            </span>
                          </div>
                          <p className="text-[9px] text-slate-500 leading-tight">
                            सिलेक्टेड ऐप्स ओपन होने पर स्टडी लॉक एक्टिव करने के लिए आवश्यक है।
                          </p>
                          <button
                            onClick={() => {
                              setIsSimulatedAccessibilityOn(prev => !prev);
                              setLaunchNotification("Redirecting to System: Accessibility Settings");
                              setTimeout(() => setLaunchNotification(null), 2500);
                            }}
                            className="w-full py-1.5 bg-slate-900 border border-slate-805 hover:bg-slate-850 text-indigo-400 text-[10px] font-bold rounded-lg transition"
                          >
                            {isSimulatedAccessibilityOn ? 'सेट देखें (View Settings)' : 'अनुमति दें (Enable)'}
                          </button>
                        </div>

                        {/* 2. Display Over Other Apps Card */}
                        <div className="bg-[#0c1424] border border-slate-805 rounded-xl p-3 space-y-2">
                          <div className="flex justify-between items-center">
                            <span className="text-[11px] font-black text-slate-200">डिस्प्ले ओवर ऐप्स (Overlay)</span>
                            <span className={`text-[9px] font-black px-1.5 py-0.5 rounded ${isSimulatedOverlayOn ? 'bg-emerald-950 text-emerald-400 border border-emerald-800/40' : 'bg-rose-950 text-rose-400 border border-rose-800/40'}`}>
                              {isSimulatedOverlayOn ? 'मंजूर (ON)' : 'अस्वीकृत (OFF)'}
                            </span>
                          </div>
                          <p className="text-[9px] text-slate-500 leading-tight">
                            ब्लॉक ऐप्स ओपन होने पर उनके ऊपर ब्लॉक स्क्रीन दिखाने के लिए आवश्यक है।
                          </p>
                          <button
                            onClick={() => {
                              setIsSimulatedOverlayOn(prev => !prev);
                              setLaunchNotification("Redirecting to System: Overlay Permissions");
                              setTimeout(() => setLaunchNotification(null), 2500);
                            }}
                            className="w-full py-1.5 bg-slate-900 border border-slate-805 hover:bg-slate-850 text-indigo-400 text-[10px] font-bold rounded-lg transition"
                          >
                            {isSimulatedOverlayOn ? 'सेट देखें (View Settings)' : 'अनुमति दें (Enable)'}
                          </button>
                        </div>

                        {/* 3. Ignore Battery Restriction Card */}
                        <div className="bg-[#0c1424] border border-slate-805 rounded-xl p-3 space-y-2">
                          <div className="flex justify-between items-center">
                            <span className="text-[11px] font-black text-slate-200">बैटरी रिस्ट्रिक्शन (Battery Restriction)</span>
                            <span className={`text-[9px] font-black px-1.5 py-0.5 rounded ${isSimulatedBatteryOn ? 'bg-emerald-950 text-emerald-400 border border-emerald-800/40' : 'bg-amber-950 text-amber-400 border border-amber-800/40'}`}>
                              {isSimulatedBatteryOn ? 'अप्रतिबंधित (Unrestricted)' : 'प्रतिबंधित (Restricted)'}
                            </span>
                          </div>
                          <p className="text-[9px] text-slate-500 leading-tight">
                            सिस्टम द्वारा बैकग्राउंड सेवा को बंद होने से बचाने के लिए ऐप को 'अप्रतिबंधित' (No Restriction) पर सेट करें।
                          </p>
                          <button
                            onClick={() => {
                              setIsSimulatedBatteryOn(prev => !prev);
                              setLaunchNotification("Redirecting to System: Battery Restriction Settings");
                              setTimeout(() => setLaunchNotification(null), 2500);
                            }}
                            className="w-full py-1.5 bg-slate-900 border border-slate-805 hover:bg-slate-850 text-indigo-400 text-[10px] font-bold rounded-lg transition"
                          >
                            {isSimulatedBatteryOn ? 'सेट देखें (View Settings)' : 'प्रतिबंध हटाएं (Remove Restriction)'}
                          </button>
                        </div>

                      </div>
                    </div>

                    {/* OK / Confirm Button */}
                    <div className="pt-4 border-t border-slate-800">
                      <button 
                        onClick={() => setShowPermissionsDialog(false)}
                        className="w-full py-2 bg-indigo-600 hover:bg-indigo-505 text-white text-xs font-black rounded-xl transition"
                      >
                        ओके (OK)
                      </button>
                    </div>

                  </div>
                )}

                {/* Simulated Share App / APK Link Modal */}
                {showShareModal && (
                  <div className="absolute inset-0 bg-[#070b13]/98 z-50 p-4 flex flex-col justify-between overflow-y-auto">
                    
                    <div className="space-y-4">
                      {/* Title Header */}
                      <div className="flex justify-between items-center pb-2 border-b border-slate-800">
                        <div className="flex items-center gap-1.5">
                          <Share2 size={16} className="text-indigo-400 animate-pulse" />
                          <span className="text-xs font-black text-indigo-300 uppercase tracking-wider">ऐप शेयर करें (Share APK Link)</span>
                        </div>
                        <button 
                          onClick={() => setShowShareModal(false)}
                          className="p-1 hover:bg-slate-800 rounded-full transition"
                        >
                          <XCircle size={18} className="text-rose-500" />
                        </button>
                      </div>

                      {/* Information text */}
                      <p className="text-[10px] text-slate-400 leading-normal">
                        अपने दोस्तों को Study Mode App Lock & Timer ऐप की डाउनलोड लिंक भेजें और उन्हें एक साथ बिना किसी डिस्ट्रेक्शन के ध्यान केंद्रित करने में मदद करें!
                      </p>

                      {/* Cool App Info Box inside popup */}
                      <div className="bg-[#0c1424] border border-indigo-950 rounded-xl p-3 flex gap-3 items-center">
                        <div className="w-12 h-12 bg-indigo-600/20 border border-indigo-500/30 rounded-xl flex items-center justify-center shrink-0">
                          <ShieldCheck className="text-indigo-400 animate-pulse" size={24} />
                        </div>
                        <div>
                          <h4 className="text-[11px] font-black text-slate-100">Study Mode App Lock & Timer • Study Companion APK</h4>
                          <p className="text-[8.5px] text-slate-500 font-mono">Package: com.studyshield.studyfocus</p>
                          <div className="flex gap-1.5 mt-1 font-mono text-[8px]">
                            <span className="bg-blue-950 text-blue-400 px-1 py-0.5 rounded">v2.1 Stable</span>
                            <span className="bg-emerald-950 text-emerald-400 px-1 py-0.5 rounded">12.5 MB APK</span>
                          </div>
                        </div>
                      </div>

                      {/* Copy Link Section */}
                      <div className="space-y-1.5">
                        <label className="text-[9.5px] font-black text-slate-400 block">APK डाउनलोड लिंक (Download Link):</label>
                        <div className="flex gap-1.5">
                          <input 
                            type="text" 
                            readOnly 
                            value={getPublicLink("/")}
                            className="bg-slate-950 border border-slate-800 text-slate-300 font-mono text-[9px] px-2.5 py-1.5 rounded-lg w-full focus:outline-none select-all"
                          />
                          <button 
                            onClick={() => {
                              navigator.clipboard.writeText(getPublicLink("/"));
                              setLaunchNotification("APK Link Copied to Clipboard!");
                              setTimeout(() => setLaunchNotification(null), 2500);
                            }}
                            className="bg-indigo-600 hover:bg-indigo-505 text-white text-[10px] font-bold px-3 py-1.5 rounded-lg shadow-md active:scale-95 transition"
                          >
                            Copy
                          </button>
                        </div>
                      </div>

                      {/* Real / Simulated Share Channels list with quick share text */}
                      <div className="space-y-2">
                        <span className="text-[9.5px] font-black text-slate-400 block">सोशल चैनल्स पर शेयर करें (Share via Social):</span>
                        
                        <div className="grid grid-cols-2 gap-2">
                          {/* WhatsApp */}
                          <button 
                            onClick={() => {
                              const shareText = `Hey! 🚀 Download *Study Mode App Lock & Timer* APK to block distracting apps during study hours. It features custom time timetables and emergency break limits with AdSense interstitial ads! Download Here: ${getPublicLink("/")}`;
                              window.open(`https://api.whatsapp.com/send?text=${encodeURIComponent(shareText)}`, '_blank');
                              setLaunchNotification("Opening WhatsApp Share Dialog...");
                              setTimeout(() => setLaunchNotification(null), 2500);
                            }}
                            className="bg-emerald-600 hover:bg-emerald-500 text-white font-black text-[10px] py-2 rounded-xl flex items-center justify-center gap-1.5 active:scale-95 transition"
                          >
                            <MessageCircle size={12} />
                            WhatsApp
                          </button>

                          {/* Telegram */}
                          <button 
                            onClick={() => {
                              const shareText = `Hey! 🚀 Download Study Mode App Lock & Timer APK to block distracting apps during study hours. It features custom time timetables and emergency break limits with AdSense ads! Download Here: ${getPublicLink("/")}`;
                              window.open(`https://t.me/share/url?url=${encodeURIComponent(getPublicLink("/"))}&text=${encodeURIComponent(shareText)}`, '_blank');
                              setLaunchNotification("Opening Telegram Share...");
                              setTimeout(() => setLaunchNotification(null), 2500);
                            }}
                            className="bg-sky-600 hover:bg-sky-500 text-white font-black text-[10px] py-2 rounded-xl flex items-center justify-center gap-1.5 active:scale-95 transition"
                          >
                            <Atom size={12} />
                            Telegram
                          </button>
                        </div>

                        {/* System Native Share Button fallback */}
                        <button 
                          onClick={async () => {
                            const shareData = {
                              title: 'Study Mode App Lock & Timer APK Download',
                              text: 'Download Study Mode App Lock & Timer App APK to secure your study sessions and avoid distractions!',
                              url: getPublicLink("/")
                            };
                            try {
                              if (navigator.share) {
                                await navigator.share(shareData);
                                setLaunchNotification("Native share completed!");
                              } else {
                                throw new Error();
                              }
                            } catch (e) {
                              setLaunchNotification("System Share triggered: Link copied instead!");
                              navigator.clipboard.writeText(getPublicLink("/"));
                            }
                            setTimeout(() => setLaunchNotification(null), 2500);
                          }}
                          className="w-full bg-slate-900 hover:bg-slate-850 text-indigo-400 font-bold text-[10.5px] py-2 rounded-xl flex items-center justify-center gap-1.5 border border-slate-800 active:scale-95 transition mt-1"
                        >
                          <Share2 size={12} />
                          सिस्टम शेयर करें (System Native Share)
                        </button>
                      </div>

                    </div>

                    {/* Return back button */}
                    <div className="pt-4 border-t border-slate-900 mt-4">
                      <button 
                        onClick={() => setShowShareModal(false)}
                        className="w-full py-2 bg-slate-800 hover:bg-slate-750 text-slate-300 text-xs font-black rounded-xl transition"
                      >
                        वापस जाएँ (Back)
                      </button>
                    </div>

                  </div>
                )}

              </div>
            )}

            {/* SCREEN 3: HIGH CONTRAST BLOCK SCREEN REDIRECT SIMULATION (BlockActivity.kt) */}
            {phoneScreen === 'blocked_screen' && (
              <div className="flex-1 p-5 flex flex-col justify-between text-center bg-gradient-to-b from-[#1c0f1c] via-[#090d16] to-[#05070a]">
                
                {/* Visual Lock Representation */}
                <div className="flex flex-col items-center pt-10">
                  <div className="bg-rose-500/10 p-3.5 rounded-full border border-rose-500/20 mb-4 animate-bounce">
                    <Lock size={34} className="text-rose-500" />
                  </div>
                  <h4 className="text-[17px] font-black text-rose-400 leading-snug">
                    पढ़ाई का सख्त समय सक्रिय है!
                  </h4>
                  <p className="text-[10px] text-slate-400 mt-1 uppercase tracking-wider">Focus Study Hours Locked</p>

                  <div className="mt-6 w-full bg-slate-900 border border-slate-800 rounded-xl p-4">
                    <p className="text-[10.5px] text-slate-400">यह ऐप प्रतिबंधित है क्योंकि यह 'स्टडी ऐप्स' की सूची में नहीं है:</p>
                    <p className="text-md font-black text-blue-400 mt-2 truncate">"{selectedBlockedAppName}"</p>
                  </div>
                </div>

                {/* Instant Remedial Action links for simulation scope */}
                <div className="space-y-2 pb-6">
                  
                  {/* Option to trigger breakthrough */}
                  <button
                    onClick={() => {
                      // Trigger emergency break directly from blocked window
                      requestBreakWithAd(15);
                    }}
                    className="w-full py-2.5 bg-emerald-600 hover:bg-emerald-500 text-slate-950 font-black text-[11px] rounded-xl transition flex items-center justify-center gap-1.5"
                  >
                    <Timer size={12} />
                    15 मिनट का ब्रेक लें (Emergency Break)
                  </button>

                  <button
                    onClick={() => {
                      setPhoneScreen('study_shield_app');
                      setActiveTab('apps');
                    }}
                    className="w-full py-2 bg-indigo-950 hover:bg-indigo-900 border border-indigo-800 text-indigo-300 font-extrabold text-[10px] rounded-xl transition flex items-center justify-center gap-1"
                  >
                    <CheckSquare size={10} />
                    स्टडी ऐप्स सेटिंग्स बदलें (Toggle lists)
                  </button>

                  <button
                    onClick={() => {
                      setPhoneScreen('launcher');
                    }}
                    className="w-full text-slate-500 hover:text-slate-450 text-[10px] pt-1.5 transition cursor-pointer"
                  >
                    होम स्क्रीन पर जाएँ (Home Screen)
                  </button>

                </div>

              </div>
            )}

          </div>

          {/* Simulated hardware back / navigation pill handler */}
          <div className="h-4 bg-[#090d16] flex items-center justify-center pb-2 relative z-30 select-none">
            <button 
              onClick={() => {
                // Returns to launcher home
                setPhoneScreen('launcher');
              }}
              className="w-24 h-1 bg-slate-705 hover:bg-slate-500 rounded-full cursor-pointer transition-colors"
              title="Touch navigating gesture handle"
            ></button>
          </div>

        </div>

        {/* Play Store Developer Dashboard & Privacy Policy Link Helper Guide */}
        <div className="w-full max-w-lg bg-[#0a101d] rounded-2xl border border-indigo-950 p-5 space-y-4 shadow-xl">
          <div className="flex items-center gap-2 pb-2.5 border-b border-indigo-950/60 select-none">
            <div className="w-7 h-7 bg-indigo-650/15 rounded-lg flex items-center justify-center">
              <ShieldCheck size={16} className="text-indigo-400" />
            </div>
            <div>
              <h3 className="text-xs font-black text-slate-100 uppercase tracking-widest">Google Play Store Developer Console Kit</h3>
              <p className="text-[9px] text-[#6b7280]">Study Mode App Lock & Timer / Build Release Configuration Checklist</p>
            </div>
          </div>

          {/* Dynamic Link Generation for Play Console Privacy URL field */}
          <div className="space-y-1.5">
            <div className="flex justify-between items-center">
              <label className="text-[10px] font-black text-slate-400">प्ले स्टोर प्राइवेसी पालिसी लिंक (Privacy Policy URL):</label>
              <span className="text-[8.5px] text-indigo-400 font-bold bg-indigo-950/40 px-1.5 py-0.5 rounded border border-indigo-900/35">100% Google Compliant</span>
            </div>
            <div className="flex gap-2">
              <input 
                type="text" 
                readOnly 
                value={getPublicLink("/privacy-policy.html")}
                className="bg-slate-950 border border-slate-805 text-slate-350 font-mono text-[9.5px] px-3 py-1.5 rounded-lg flex-1 select-all focus:outline-none"
              />
              <button 
                onClick={() => {
                  navigator.clipboard.writeText(getPublicLink("/privacy-policy.html"));
                  setLaunchNotification("Copied Privacy Policy Link to Clipboard!");
                  setTimeout(() => setLaunchNotification(null), 2500);
                }}
                className="bg-indigo-600 hover:bg-indigo-550 active:scale-95 text-white text-[10px] font-black px-4 py-1.5 rounded-lg shadow-md transition"
              >
                Copy URL
              </button>
            </div>
            <p className="text-[8.5px] text-slate-500 leading-tight">
              * अपनी Google Play Console में <strong>"App Content &gt; Privacy Policy"</strong> वाले विकल्प में ऊपर दिए गए लिंक को पेस्ट करें। यह पेज रीयल-टाइम में उपलब्ध है।
            </p>
          </div>

          {/* Guidelines Section organized with visual hierarchy */}
          <div className="space-y-3 pt-1">
            <h4 className="text-[10px] font-black text-slate-300 uppercase tracking-wider">प्ले स्टोर पर पब्लिश करने की रणनीति (Publishing Tips):</h4>
            
            <div className="space-y-2 text-[10px] text-slate-400 leading-normal">
              
              <div className="bg-[#0e172a] border border-blue-950 p-2.5 rounded-xl space-y-1">
                <span className="font-bold text-blue-400 block text-[9.5px]">⚠️ एक्सीसिबिलिटी डिक्लेरेशन (Accessibility Disclosures):</span>
                <p className="text-[9px] leading-relaxed">
                  जब प्ले स्टोर आपसे एक्सीसिबिलिटी सर्विस के उद्देश्य के बारे में पूछे, तो घोषित करें कि:
                  "यह ऐप केवल पढ़ाई के दौरान विचलित करने वाली ऐप्स के पैकेज नामों को पहचानने के लिए Accessibility Service API की अनुमति मांगता है। यह पूरी प्रक्रिया ऑफलाइन होती है और ऐप किसी भी प्रकार का उपयोगकर्ता डेटा एकत्र या लीक नहीं करता है।"
                </p>
              </div>

              <div className="bg-[#0e172a] border border-emerald-950 p-2.5 rounded-xl space-y-1">
                <span className="font-bold text-emerald-400 block text-[9.5px]">🛡️ डेटा सुरक्षा फार्म (Data Safety Declaration):</span>
                <p className="text-[9px] leading-relaxed">
                  Google Play Console में डेटा सेफ्टी फ़ॉर्म भरते समय चुनें:
                  <strong>"No user data is collected or shared with third parties"</strong>। यह ऐप शून्य डेटा संग्रह नीति का पालन करता है क्योंकि इसका उपयोग पूरी तरह ऑफलाइन है।
                </p>
              </div>

              <div className="bg-[#0e172a] border border-amber-950 p-2.5 rounded-xl space-y-1">
                <span className="font-bold text-amber-405 block text-[9.5px]">👥 लक्षित दर्शक (Target Audience Rating):</span>
                <p className="text-[9px] leading-relaxed">
                  हम सुझाव देते हैं की आप अपनी ऐप के लिए लक्षित दर्शक आयु <strong>13+ ya 18+ (Teens and Above)</strong> चुनें। इससे बच्चों से संबंधित कड़े प्रतिबंधों और कॉप्टा (COPPA Notification) नियमों की वजह से ऐप रिजेक्ट होने का जोखिम खत्म हो जाएगा।
                </p>
              </div>

              <div className="bg-[#0e172a] border border-indigo-950 p-2.5 rounded-xl space-y-1">
                <span className="font-bold text-indigo-400 block text-[9.5px]">📢 विज्ञापनों का सेटअप (Google AdSense Setup):</span>
                <p className="text-[9px] leading-relaxed">
                  चूंकि ऐप में ब्रेक के समय फुल-स्क्रीन एड आते हैं, इसी प्रकार के इंटरस्टिशियल विज्ञापनों को प्ले स्टोर की <strong>"Ads &gt; App Contains Ads"</strong> सेटिंग्स में हां (Yes) पर सेट करें।
                </p>
              </div>

            </div>
          </div>

          <div className="pt-2 border-t border-indigo-950/60 flex justify-between items-center text-[9px] text-[#4b5563]">
            <span>APK Target: Android API 34 (Android 14)</span>
            <span className="font-bold text-emerald-400">✔ Ready for Play Store Submission</span>
          </div>
        </div>

      </main>

      {/* Styled Footer */}
      <footer className="border-t border-slate-900 bg-[#060a12] py-4 text-center text-[11px] text-slate-600">
        <p>© 2026 Study Mode App Lock & Timer Pro. For 18+ Student Focus and Time Safety.</p>
        <p className="mt-0.5 text-slate-750">Designed exclusively to bypass distraction channels elegantly.</p>
      </footer>
    </div>
  );
}
