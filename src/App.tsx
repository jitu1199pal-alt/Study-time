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
  Chrome
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
                    
                    {/* Core StudyShield Icon */}
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
                      <span className="text-[9.5px] text-indigo-200 font-extrabold mt-1 truncate w-full text-center">StudyShield</span>
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

            {/* SCREEN 2: STUDYSHIELD APPLICATION SUITE (Main simulated APK contents) */}
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
                    <button
                      onClick={() => setShowCustomBreakSelector(true)}
                      className="px-2.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 active:scale-95 text-slate-950 font-extrabold text-[11px] rounded-lg shadow-md flex items-center gap-1 transition"
                    >
                      <Timer size={12} />
                      Break लें
                    </button>
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
                          onClick={() => handleActivateBreak(customBreakDuration)}
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

                      {/* Expandable Privacy Policy Widget */}
                      <div 
                        className="bg-slate-950/40 border border-indigo-950 rounded-xl p-3 cursor-pointer select-none text-left transition hover:border-indigo-900"
                        onClick={() => setIsPolicyExpanded(!isPolicyExpanded)}
                      >
                        <div className="flex justify-between items-center text-[11px] font-bold text-indigo-400">
                          <span className="flex items-center gap-1.5">
                            🔒 सुरक्षा एवं गोपनीयता नीति
                          </span>
                          <span className="text-[10px] text-slate-500 font-normal">
                            {isPolicyExpanded ? 'छुपाएं ▲' : 'देखें ▼'}
                          </span>
                        </div>
                        <p className="text-[10px] text-slate-400 mt-1.5 leading-normal">
                          यह ऐप पूरी तरह से ऑफलाइन और सुरक्षित है। हम आपका कोई भी पर्सनल या ब्राउज़िंग डेटा किसी बाहरी सर्वर पर नहीं भेजते हैं।
                        </p>
                        {isPolicyExpanded && (
                          <div className="mt-2 pt-2 border-t border-indigo-950 space-y-1.5 text-[9px] text-slate-450 leading-relaxed">
                            <p>
                              <strong className="text-white block">1. अभिगम्यता सेवा (Accessibility API):</strong>
                              यह अनुमति केवल सक्रिय ऐप के पैकेज नाम की जाँच करने और विचलित करने वाले ऐप्स को आपके निर्धारित समय पर स्थानीय रूप से ब्लॉक करने के लिए चुनी जाती है।
                            </p>
                            <p>
                              <strong className="text-white block">2. 100% ऑफलाइन सुरक्षा (Data Safety):</strong>
                              आपके फ़ोन से कोई भी डेटा बाहरी सर्वर, विज्ञापनदाता या तृतीय पक्षों के साथ शेयर नहीं किया जाता है। सभी शेड्यूल्स और प्राथमिकताएं स्थानीय रूप से एन्क्रिप्टेड स्टोरेज में संग्रहीत होती हैं।
                            </p>
                            <p>
                              <strong className="text-indigo-400 block font-semibold">3. Play Store Policy Compliance:</strong>
                              StudyShield strictly obeys Google Play Policies regarding sensitive user permissions (Accessibility API disclosures).
                            </p>
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

                        {/* 3. Ignore Battery Optimization Card */}
                        <div className="bg-[#0c1424] border border-slate-805 rounded-xl p-3 space-y-2">
                          <div className="flex justify-between items-center">
                            <span className="text-[11px] font-black text-slate-200">बैटरी ऑप्टिमाइजेशन (Battery)</span>
                            <span className={`text-[9px] font-black px-1.5 py-0.5 rounded ${isSimulatedBatteryOn ? 'bg-emerald-950 text-emerald-400 border border-emerald-800/40' : 'bg-amber-950 text-amber-400 border border-amber-800/40'}`}>
                              {isSimulatedBatteryOn ? 'अनुकूलित (Ignored)' : 'सक्रिय (Saver)'}
                            </span>
                          </div>
                          <p className="text-[9px] text-slate-500 leading-tight">
                            सिस्टम द्वारा बैकग्राउंड सेवा को बंद होने से बचाने के लिए प्ले-स्टोर सुरक्षा नियम कंपैटिबल।
                          </p>
                          <button
                            onClick={() => {
                              setIsSimulatedBatteryOn(prev => !prev);
                              setLaunchNotification("Redirecting to System: Battery Optimization Settings");
                              setTimeout(() => setLaunchNotification(null), 2500);
                            }}
                            className="w-full py-1.5 bg-slate-900 border border-slate-805 hover:bg-slate-850 text-indigo-400 text-[10px] font-bold rounded-lg transition"
                          >
                            {isSimulatedBatteryOn ? 'सेट देखें (View Settings)' : 'अनुकूलन बंद करें (Ignore)'}
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
                      handleActivateBreak(15);
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

      </main>

      {/* Styled Footer */}
      <footer className="border-t border-slate-900 bg-[#060a12] py-4 text-center text-[11px] text-slate-600">
        <p>© 2026 StudyShield Pro. For 18+ Student Focus and Time Safety.</p>
        <p className="mt-0.5 text-slate-750">Designed exclusively to bypass distraction channels elegantly.</p>
      </footer>
    </div>
  );
}
